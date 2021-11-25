package ru.uao.chef.assistant.ui.product

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.uao.chef.assistant.MainActivity
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.databinding.FragmentProductStoreBinding
import ru.uao.chef.assistant.ui.product.data.Product
import ru.uao.chef.assistant.ui.product.view.ProductAdapter

class ProductStoreFragment : Fragment() {

    //private lateinit var productStoreViewModel: ProductStoreViewModel
    private var _binding: FragmentProductStoreBinding? = null

    private lateinit var addProductBtn: FloatingActionButton
    private lateinit var saveBtn: Button
    private lateinit var productList: ArrayList<Product>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recv: RecyclerView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    companion object {

        fun saveData(context: Context){
            Toast.makeText(context,"saveData", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //productStoreViewModel =
         //   ViewModelProvider(this).get(ProductStoreViewModel::class.java)

        _binding = FragmentProductStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textSlideshow
        productStoreViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        addProductBtn = binding.addingProductBtn
        saveBtn = binding.BtnSave
        productList = ArrayList()
        recv = binding.mRecycler
        productAdapter = ProductAdapter(root.context, productList)
        recv.layoutManager = LinearLayoutManager(root.context)
        recv.adapter = productAdapter
        addProductBtn.setOnClickListener {
            addProduct(root.context)
        }
        saveBtn.setOnClickListener {

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addProduct(context: Context) {
        hideKeyboard()
        val inflter = LayoutInflater.from(context)
        val v = inflter.inflate(R.layout.add_product_item, null)
        val productName = v.findViewById<EditText>(R.id.productName)
        val weightProduct = v.findViewById<EditText>(R.id.weightProduct)
        val priceProduct = v.findViewById<EditText>(R.id.priceProduct)

        val addDialog = AlertDialog.Builder(context)

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            when {
                "" == productName.text.toString() -> {
                    Toast.makeText(context, "Set param product Name", Toast.LENGTH_SHORT).show()
                }
                "" == weightProduct.text.toString() -> {
                    Toast.makeText(context, "Set param weight Product", Toast.LENGTH_SHORT).show()
                }
                "" == priceProduct.text.toString() -> {
                    Toast.makeText(context, "Set param price Product", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val productName = productName.text.toString()
                    val weightProduct = weightProduct.text.toString().toFloat()
                    val priceProduct = priceProduct.text.toString().toFloat()
                    val w = Product(productName, weightProduct, priceProduct)
                    productList.add(w)
                    productAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "Adding User Information Success", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                }
            }
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show()

        }
        addDialog.create()
        addDialog.show()
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}