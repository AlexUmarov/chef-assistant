package ru.uao.chef.assistant.ui.product

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.databinding.FragmentProductStoreBinding
import ru.uao.chef.assistant.ui.product.data.Product
import ru.uao.chef.assistant.ui.product.view.ProductAdapter
import kotlin.collections.ArrayList

class ProductStoreFragment : Fragment(), ProductAdapter.OnItemClickListener {

    //private lateinit var productStoreViewModel: ProductStoreViewModel
    private var _binding: FragmentProductStoreBinding? = null

    private lateinit var addProductBtn: FloatingActionButton
    private lateinit var saveBtn: Button
    private lateinit var productList: ArrayList<Product>
    private lateinit var productNewList: ArrayList<Product>
    private lateinit var productDeleteList: ArrayList<Product>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recv: RecyclerView

    private lateinit var ctx: Context

    private lateinit var auth: FirebaseAuth
    private var fireBase = Firebase.firestore

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    /*companion object {

        fun saveData(context: Context){
            Toast.makeText(context,"saveData", Toast.LENGTH_SHORT).show()
        }
    }*/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //productStoreViewModel =
         //   ViewModelProvider(this).get(ProductStoreViewModel::class.java)

        _binding = FragmentProductStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root
        ctx = root.context

        /*val textView: TextView = binding.textSlideshow
        productStoreViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        FirebaseApp.initializeApp(root.context)
        auth = FirebaseAuth.getInstance()


        addProductBtn = binding.addingProductBtn
        saveBtn = binding.BtnSave
        productList = ArrayList()
        productNewList = ArrayList()
        productDeleteList = ArrayList()
        recv = binding.mRecycler
        productAdapter = ProductAdapter(root.context, productList, productDeleteList, this)

        recv.layoutManager = LinearLayoutManager(root.context)
        recv.adapter = productAdapter
        getWorkoutInfo()
        addProductBtn.setOnClickListener {
            addProduct(root.context)
        }
        saveBtn.setOnClickListener {
            saveWorkoutInfo(root.context)
        }

        return root
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(
            context, "productList ${productList[position].productName} !",
            Toast.LENGTH_SHORT
        ).show()

        val inflter = LayoutInflater.from(ctx)
        val v = inflter.inflate(R.layout.add_product_item_to_cart, null)
        val addDialog = AlertDialog.Builder(ctx)
        addDialog.setView(v)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show()

        }
        addDialog.create()
        addDialog.show()

    }

    private fun saveWorkoutInfo(context: Context) {
        hideKeyboard()

        productDeleteList?.forEach {
            fireBase.collection(auth.currentUser?.email.toString())
                .document("ProductStore")
                .collection("current")
                .document(it.productName)
                .delete()
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        context, "Document ${it.productName} successfully deleted!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { documentReference ->
                    Toast.makeText(
                        context, "Error deleting document ${it.productName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        productNewList.forEach {
            fireBase.collection(auth.currentUser?.email.toString())
                .document("ProductStore")
                .collection("current")
                .document(it.productName)
                .set(it)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        context, "Add product: ${it.productName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context, "Error adding product: $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        productDeleteList.clear()
        productNewList.clear()
    }

    private fun getWorkoutInfo() {
        fireBase.collection(auth.currentUser?.email.toString())
            .document("ProductStore")
            .collection("current")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                productDeleteList.clear()
                for (document in result) {
                    val w = Product(document.data["productName"].toString(),
                        document.data["productWeight"].toString().toFloat(),
                        document.data["productPrice"].toString().toFloat())
                    productList.add(w)
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting product.", exception)
            }
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
                    productNewList.add(w)
                    productList.add(w)
                    productAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "Adding User Information Success", Toast.LENGTH_SHORT)
                        .show()
                    saveWorkoutInfo(context)
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