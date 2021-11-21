package ru.uao.chef.assistant.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ru.uao.chef.assistant.databinding.FragmentProductStoreBinding

class ProductStoreFragment : Fragment() {

    private lateinit var productStoreViewModel: ProductStoreViewModel
    private var _binding: FragmentProductStoreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        productStoreViewModel =
            ViewModelProvider(this).get(ProductStoreViewModel::class.java)

        _binding = FragmentProductStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        productStoreViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}