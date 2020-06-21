package de.tolunla.ghostotp.view.fragment

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import de.tolunla.ghostotp.databinding.FragmentBarcodeBinding
import de.tolunla.ghostotp.util.AccountUtils
import java.net.URLDecoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeFragment : Fragment() {
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private lateinit var binding: FragmentBarcodeBinding
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val TAG = "BarcodeFragment"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder().build()

            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        val data = URLDecoder.decode(barcode.rawValue, "UTF-8")
                        AccountUtils.accountFromUri(Uri.parse(data))
                    })
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                preview?.setSurfaceProvider(
                    binding.viewFinder.createSurfaceProvider(camera?.cameraInfo)
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        when (checkSelfPermission(requireContext(), it)) {
            PermissionChecker.PERMISSION_GRANTED -> true
            else -> false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                binding.root.findNavController().navigateUp()
            }
        }
    }

    private class BarcodeAnalyzer(private val listener: (barcode: Barcode) -> Unit) :
        ImageAnalysis.Analyzer {

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(proxy: ImageProxy) {
            val mediaImage = proxy.image

            mediaImage?.let {
                val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
                BarcodeScanning.getClient().process(image)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstOrNull {
                            it.valueType == Barcode.TYPE_TEXT
                        }?.let(listener)

                        proxy.close()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Use case binding failed", it)
                        proxy.close()
                    }
            }
        }
    }
}