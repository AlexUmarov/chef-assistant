package ru.uao.chef.assistant.ui.cart

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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.databinding.FragmentCartStoreBinding
import ru.uao.chef.assistant.ui.cart.data.Cart
import ru.uao.chef.assistant.ui.cart.view.CartListAdapter
import ru.uao.chef.assistant.ui.home.data.Recipe
import ru.uao.chef.assistant.ui.product.data.Product

class CartFragment : Fragment(), CartListAdapter.OnItemClickListener, FireBaseResultListener {

    private var _binding: FragmentCartStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var fireBase = Firebase.firestore

    private lateinit var rvCartList: RecyclerView
    private lateinit var currentCart: Cart
    private lateinit var cartNameTV: TextInputEditText
    private lateinit var cartCostTV: TextView
    private lateinit var productList: ArrayList<Product>
    private lateinit var cartListAdapter: CartListAdapter
    private lateinit var saveBtn: Button
    private lateinit var ctx: Context


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root
        ctx = root.context

        FirebaseApp.initializeApp(root.context)
        auth = FirebaseAuth.getInstance()
        cartNameTV = binding.cartName
        cartCostTV = binding.cartCostTV
        rvCartList = binding.recyclerCartList
        saveBtn = binding.BtnSave
        getCartInfo(this)
        currentCart = Cart("current", 0.0F, ArrayList())
        productList = ArrayList()

        saveBtn.setOnClickListener {
            saveCartAsRecipe()
        }

        return root
    }

    override fun onResult(isAdded: Boolean) {
        productList = currentCart.products
        cartListAdapter = CartListAdapter(productList, this)
        rvCartList.layoutManager = LinearLayoutManager(ctx)
        rvCartList.adapter = cartListAdapter
        cartNameTV.setText(currentCart.cartName)
        cartCostTV.text = currentCart.getTotalCostProduct().toString()
    }

    override fun onError(error: Throwable) {
        TODO("Not yet implemented")
    }

    private fun getCartInfo(fireBaseResultListener: FireBaseResultListener){
        fireBase.collection(auth.currentUser?.email.toString())
            .document("CartStore")
            .get()
            .addOnSuccessListener { documentReference ->
                currentCart.cartName = documentReference.data?.get("cartName").toString()
                var products = ArrayList<Product>()
                var p = documentReference.data?.get("products") as ArrayList<*>
                for (i in p.indices){
                    var p = p[i] as HashMap<*, *>
                    products.add(Product(
                        p["productName"].toString(),
                        p["productWeight"].toString().toFloat(),
                        p["productPrice"].toString().toFloat(),
                    ))
                }
                currentCart.products = products
                fireBaseResultListener.onResult(true)

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting product.", exception)
                fireBaseResultListener.onError(exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
        val addDialog = AlertDialog.Builder(ctx)
        val inflter = LayoutInflater.from(context)
        val view = inflter.inflate(R.layout.delete_product_item, null)
        view.findViewById<TextView>(R.id.deleteProductInfo).text =
            "Delete ${currentCart.products[position].productName}?"
        addDialog.setView(view)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            deleteProductFromCartList(currentCart.products[position])
        }
        addDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()
    }

    private fun deleteProductFromCartList(product: Product){
        for(i in currentCart.products.indices){
            if (currentCart.products[i].productName == product.productName) {
                currentCart.products.removeAt(i)
                break
            }
        }
        currentCart.cartName = "current"
        currentCart.totalCost = currentCart.getTotalCostProduct()
        fireBase.collection(auth.currentUser?.email.toString())
            .document("CartStore")
            .set(currentCart)
            .addOnSuccessListener {
                getCartInfo(this)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context, "Error adding product: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveCartAsRecipe(){
        hideKeyboard()
        currentCart.cartName = cartNameTV.text.toString()
        var recipe = Recipe(currentCart.cartName,
            currentCart.getTotalCostProduct(),
            currentCart.products
        )
        fireBase.collection(auth.currentUser?.email.toString())
            .document("RecipeStore")
            .collection("current")
            .document(recipe.recipeName)
            .set(recipe)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    context, "Add recipe: ${recipe.recipeName}",
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