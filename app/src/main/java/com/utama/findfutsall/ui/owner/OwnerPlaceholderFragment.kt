package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.utama.findfutsall.databinding.FragmentOwnerPlaceholderBinding

class OwnerPlaceholderFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESC  = "desc"

        fun newInstance(title: String, desc: String): OwnerPlaceholderFragment {
            return OwnerPlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, desc)
                }
            }
        }
    }

    private var _binding: FragmentOwnerPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPlaceholderIcon.text  = ""
        binding.tvPlaceholderTitle.text = arguments?.getString(ARG_TITLE) ?: "Segera Hadir"
        binding.tvPlaceholderDesc.text  = arguments?.getString(ARG_DESC) ?: "Fitur ini sedang dalam pengembangan"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}