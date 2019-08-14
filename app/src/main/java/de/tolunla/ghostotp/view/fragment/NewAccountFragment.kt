package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.FragmentNewAccountBinding
import de.tolunla.ghostotp.showSoftKeyboard
import org.apache.commons.codec.binary.Base32

class NewAccountFragment : Fragment(), TextWatcher {

  private var prev = 0  // Pointer for the previous secret key character location
  private var current = 1 // Pointer to the current secret key character location
  private val base32Chars = Regex("[A-Za-z2-7=]") // Used to match legal base32 chars

  private lateinit var binding: FragmentNewAccountBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentNewAccountBinding.inflate(layoutInflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    with(binding) {

      context?.let {

        inputAuthType.setAdapter(
          ArrayAdapter(
            it,
            R.layout.support_simple_spinner_dropdown_item,
            arrayOf(
              getString(R.string.label_time_based),
              getString(R.string.label_counter_based)
            )
          )
        )

        buttonAdd.setOnClickListener { _ ->
          if (validateSecret(true)) {
            // TODO: Neatly map user inputs to an AccountEntity
            // AppDatabase.getInstance(it).accountDao().insertAccount()
          }
        }
      }

      inputSecretKey.addTextChangedListener(this@NewAccountFragment)
      inputAuthType.setText(getString(R.string.label_time_based), false)
    }
  }

  override fun onResume() {
    super.onResume()
    // Request for the software keyboard to appear on the name EditText
    context?.let { binding.inputAccountName.showSoftKeyboard(it) }
  }

  private fun getSecretKey(): String {

    return binding.inputSecretKey.text.toString()
      .replace(" ", "")
      .toUpperCase()
  }

  private fun validateSecret(toSubmit: Boolean = false): Boolean {
    // TODO: Determine if Google's discarding of the last incomplete chunk is wanted behavior
    try {

      val bytes = Base32().decode(getSecretKey())

      if (bytes.size < 10) {

        binding.layoutKeyInput.error = (if (toSubmit) getString(
          R.string.message_key_too_short) else null)
        return false
      }

      binding.layoutKeyInput.error = null
    } catch (e: IllegalArgumentException) {

      println(e.message)

      binding.layoutKeyInput.error =
        (if (toSubmit) getString(R.string.message_key_invalid_chars) else null)

      return false
    }

    return true
  }

  override fun afterTextChanged(s: Editable?) {
    validateSecret()

    s?.let {

      if (s.isNotEmpty()) {

        val last = s[current - 1]

        if (current.rem(5) == 0 && prev < current) {
          s.replace(current - 1, current, " $last")
        }

        if (!base32Chars.matches("$last")) {
          s.delete(s.length - 1, s.length)
        }
      }
    }
  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    s?.let {
      prev = current
      current = s.length
    }
  }
}