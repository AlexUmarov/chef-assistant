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
import android.widget.*
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
import android.widget.Spinner
import android.widget.ArrayAdapter
import ru.uao.chef.assistant.ui.cart.data.Cart
import ru.uao.chef.assistant.ui.home.data.Recipe


class ProductStoreFragment : Fragment(), ProductAdapter.OnItemClickListener {

    private var _binding: FragmentProductStoreBinding? = null
    private lateinit var addProductBtn: FloatingActionButton
    private lateinit var imageButtonCart: ImageButton
    private lateinit var productList: ArrayList<Product>
    private lateinit var productNewList: ArrayList<Product>
    private lateinit var productDeleteList: ArrayList<Product>
    private lateinit var cartList: ArrayList<Cart>
    private lateinit var currentCart: Cart
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartTV: TextView
    private lateinit var recv: RecyclerView

    private lateinit var ctx: Context
    private lateinit var auth: FirebaseAuth
    private var fireBase = Firebase.firestore

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root
        ctx = root.context

        FirebaseApp.initializeApp(root.context)
        auth = FirebaseAuth.getInstance()

        cartTV = binding.cartCostTV
        addProductBtn = binding.addingProductBtn
        imageButtonCart = binding.imageButtonCart
        productList = ArrayList()
        productNewList = ArrayList()
        productDeleteList = ArrayList()
        cartList = ArrayList()
        getCartInfo()
        currentCart = Cart("current", 0.0F, ArrayList())
        cartTV.text = currentCart.getTotalCostProduct().toString()

        recv = binding.recyclerProductList
        productAdapter = ProductAdapter(root.context, productList, this)

        recv.layoutManager = LinearLayoutManager(root.context)
        recv.adapter = productAdapter
        getProductInfo()

        addProductBtn.setOnClickListener {
            addProduct()
        }

        imageButtonCart.setOnClickListener{
            /*val manager: FragmentManager = childFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            transaction.replace(R.id.nav_view, HomeFragment())
            transaction.commit()*/
        }

        return root
    }

    override fun onItemClick(position: Int) {
        val view = LayoutInflater.from(context).inflate(R.layout.add_product_item_to_cart, null)
        var tvCartNameItem: TextView = view.findViewById<EditText>(R.id.cartNameItem)
        tvCartNameItem.text = productList[position].productName
        cartTV.text = currentCart.getTotalCostProduct().toString()

        val addDialog = AlertDialog.Builder(ctx)
        addDialog.setView(view)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            val weightProduct = view.findViewById<EditText>(R.id.cartWeightProduct).text.toString().toFloat()
            var product = productList[position]
            if(product.productWeight == weightProduct){
                saveCartList(product)
                cartTV.text = currentCart.getTotalCostProduct().toString()
            }else{
                var weightSelected = (weightProduct / product.productWeight) * 100
                var price = product.productPrice * (weightSelected/100)
                product.productWeight = weightProduct
                product.productPrice = price
                saveCartList(product)
                cartTV.text = currentCart.getTotalCostProduct().toString()
            }
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()
    }

    override fun onItemEditOkClick(product: Product) {
        hideKeyboard()
        fireBase.collection(auth.currentUser?.email.toString())
            .document("ProductStore")
            .collection("current")
            .document(product.productName)
            .set(product)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    context, "Add product: ${product.productName}",
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

    override fun onItemDeleteOkClick(product: Product) {
        hideKeyboard()
        fireBase.collection(auth.currentUser?.email.toString())
            .document("ProductStore")
            .collection("current")
            .document(product.productName)
            .delete()
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    context, "Document ${product.productName} successfully deleted!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { documentReference ->
                Toast.makeText(
                    context, "Error deleting document ${product.productName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getCartInfo(){
        fireBase.collection(auth.currentUser?.email.toString())
            .document("CartStore")
            .get()
            .addOnSuccessListener { documentReference ->
                currentCart.cartName = documentReference.data?.get("cartName").toString()
                var products = ArrayList<Product>()
                var p = documentReference.data?.get("products") as ArrayList<*>
                for (i in p.indices){
                    var p = p[i] as HashMap<*, *>
                    products.add(Product(p["productName"].toString(),
                        p["productWeight"].toString().toFloat(),
                        p["productPrice"].toString().toFloat(),
                    ))
                }
                currentCart.products = products

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting product.", exception)
            }
    }

    private fun saveCartList(product: Product){
        var isReplaced = false
        for(i in currentCart.products.indices){
            if (currentCart.products[i].productName == product.productName) {
                currentCart.products[i] = product
                isReplaced = true
            }
        }
        if(!isReplaced)
            currentCart.products.add(product)
        currentCart.cartName = "current"
        currentCart.totalCost = currentCart.getTotalCostProduct()
        fireBase.collection(auth.currentUser?.email.toString())
            .document("CartStore")
            .set(currentCart)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    context, "Add product: ${currentCart.cartName}",
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

    @SuppressLint("NotifyDataSetChanged")
    private fun getProductInfo() {
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
    private fun addProduct() {
        hideKeyboard()
        val inflter = LayoutInflater.from(context)
        val v = inflter.inflate(R.layout.add_product_item, null)
        val productName = v.findViewById<EditText>(R.id.productName)
        val weightProduct = v.findViewById<EditText>(R.id.weightProduct)
        val priceProduct = v.findViewById<EditText>(R.id.priceProduct)

        val addDialog = AlertDialog.Builder(ctx)

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
                    val product = Product(productName, weightProduct, priceProduct)
                    productNewList.add(product)
                    productList.add(product)
                    productAdapter.notifyDataSetChanged()
                    onItemEditOkClick(product)
                    dialog.dismiss()
                }
            }
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
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