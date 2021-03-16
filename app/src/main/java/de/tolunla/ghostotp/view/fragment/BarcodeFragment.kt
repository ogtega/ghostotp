package de.tolunla.ghostotp.view.fragment

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import de.tolunla.ghostotp.databinding.FragmentBarcodeBinding
import de.tolunla.ghostotp.util.accountFromUri
import de.tolunla.ghostotp.viewmodel.AccountViewModel
import java.net.URLDecoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Fragment used to scan new otp accounts
 */
class BarcodeFragment : Fragment() {
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    private lateinit var binding: FragmentBarcodeBinding
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val TAG = "BarcodeFragment"
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            accountViewModel = ViewModelProvider(it).get(AccountViewModel::class.java)
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
                if (permission) {
                    startCamera()
                } else {
                    binding.root.findNavController().navigateUp()
                }
            }.launch(CAMERA_PERMISSION)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBarcodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

                imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            BarcodeAnalyzer { barcode ->
                                val data = URLDecoder.decode(barcode.rawValue, "UTF-8")
                                accountFromUri(Uri.parse(data))?.let { account ->
                                    accountViewModel.insert(account)
                                    it.clearAnalyzer()
                                    binding.root.findNavController().navigateUp()
                                }
                            }
                        )
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
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }
            },
            getMainExecutor(requireContext())
        )
    }

    private fun allPermissionsGranted() =
        when (checkSelfPermission(requireContext(), CAMERA_PERMISSION)) {
            PermissionChecker.PERMISSION_GRANTED -> true
            else -> false
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
