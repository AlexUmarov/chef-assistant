package ru.uao.chef.assistant.ui.cakes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.uao.chef.assistant.databinding.FragmentSpongeCakesBinding

class SpongeCakesFragment : Fragment() {

    private lateinit var spongeCakesViewModel: SpongeCakesViewModel
    private var _binding: FragmentSpongeCakesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        spongeCakesViewModel =
            ViewModelProvider(this).get(SpongeCakesViewModel::class.java)

        _binding = FragmentSpongeCakesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        spongeCakesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}