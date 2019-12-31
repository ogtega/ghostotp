package de.tolunla.ghostotp.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.FragmentNewPlatformBinding

class NewPlatformFragment : Fragment() {

  private var platform = -1
  private lateinit var binding: FragmentNewPlatformBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      platform = it.getInt("dest")
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    binding = FragmentNewPlatformBinding.inflate(inflater, container, false)
    return binding.root
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.webView.settings.javaScriptEnabled = true

    binding.webView.loadUrl(when (platform) {
      R.id.new_steam_dest -> "file:///android_asset/steamauth/index.html"
      else -> ""
    })
  }
}