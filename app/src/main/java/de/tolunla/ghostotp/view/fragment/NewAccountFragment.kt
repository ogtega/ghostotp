package de.tolunla.ghostotp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import de.tolunla.ghostotp.R
import de.tolunla.ghostotp.databinding.FragmentNewAccountBinding

class NewAccountFragment : Fragment() {

    lateinit var binding: FragmentNewAccountBinding

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

        binding.inputAuthType.setAdapter(
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

        binding.inputAuthType.setText(getString(R.string.label_time_based), false)
    }
}