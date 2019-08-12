package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.FragmentNewAccountBinding
import de.tolunla.ghostotp.showSoftKeyboard
import org.apache.commons.codec.binary.Base32

class NewAccountFragment : Fragment(), TextWatcher {

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
            inputAuthType.setAdapter(
                context?.let {
                    ArrayAdapter(
                        it,
                        R.layout.support_simple_spinner_dropdown_item,
                        arrayOf(
                            getString(R.string.label_time_based),
                            getString(R.string.label_counter_based)
                        )
                    )
                }
            )

            inputSecretKey.bas32Filter()
            inputAuthType.setText(getString(R.string.label_time_based), false)
            inputSecretKey.addTextChangedListener(this@NewAccountFragment)

            buttonAdd.setOnClickListener {
                if (validateSecret(true)) {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Request for the software keyboard to appear on the name EditText
        context?.let { binding.inputAccountName.showSoftKeyboard(it) }
    }

    private fun getSecretKey(): String {
        return binding.inputSecretKey.text.toString().toUpperCase()
    }

    private fun validateSecret(toSubmit: Boolean = false): Boolean {
        // TODO: Determine if Google's discarding of the last incomplete chunk is wanted
        try {
            val bytes = Base32().decode(getSecretKey())

            if (bytes.size < 10) {
                binding.layoutKeyInput.error =
                    (if (toSubmit) getString(R.string.message_key_too_short) else null)
                return false
            }

            binding.layoutKeyInput.error = null
            return true
        } catch (t: Throwable) {
            println(t.message)
            binding.layoutKeyInput.error =
                (if (toSubmit) getString(R.string.message_key_invalid_chars) else null)
            return false
        }
    }

    override fun afterTextChanged(s: Editable?) {
        validateSecret()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    // Filters all text not allowed in a Base32 encoded string
    private fun TextInputEditText.bas32Filter() {
        filters = filters.plus(
            listOf(InputFilter { s, _, _, _, _, _ ->
                s.replace(Regex("[^A-Za-z2-7\\s=]"), "")
            })
        )
    }
}