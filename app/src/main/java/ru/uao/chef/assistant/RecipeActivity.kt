package ru.uao.chef.assistant

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Document
import ru.uao.chef.assistant.databinding.ActivityMainBinding
import ru.uao.chef.assistant.ui.home.data.Recipe
import ru.uao.chef.assistant.ui.product.data.Product
import ru.uao.chef.assistant.ui.product.view.ProductAdapter

class RecipeActivity : AppCompatActivity(), ProductAdapter.OnItemClickListener {

    private lateinit var recyclerRecipeActivityList: RecyclerView
    private lateinit var recipeActivityRecipeName: TextView
    private lateinit var totalWeightInfo: TextView
    private lateinit var totalPriceInfo: TextView
    private lateinit var auth: FirebaseAuth
    private var fireBase = Firebase.firestore
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>
    private lateinit var recipeName: String
    private lateinit var currentRecipe: Recipe


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        recyclerRecipeActivityList = findViewById(R.id.recyclerRecipeActivityList)
        recipeActivityRecipeName = findViewById(R.id.recipeActivityRecipeName)
        totalWeightInfo = findViewById(R.id.totalWeightInfo)
        totalPriceInfo = findViewById(R.id.totalPriceInfo)

        var extras = intent.extras
        recipeName = extras?.getString("RecipeName").toString()
        recipeActivityRecipeName.text = recipeName

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        productList = ArrayList()

        productAdapter = ProductAdapter(this, productList, this)
        recyclerRecipeActivityList.layoutManager = LinearLayoutManager(this)
        recyclerRecipeActivityList.adapter = productAdapter
        getRecipeInfo()


    }

    override fun onItemClick(position: Int) {

    }

    override fun onItemEditOkClick(product: Product) {
        hideKeyboard()
        saveChangeRecipe(product)
    }

    override fun onItemDeleteOkClick(product: Product) {
        hideKeyboard()
        saveChangeRecipe(product)
    }

    private fun saveChangeRecipe(product: Product){
        for(i in currentRecipe.products.indices){
            if(currentRecipe.products[i].productName == product.productName){
                currentRecipe.products.removeAt(i)
            }
        }
        currentRecipe.getTotalCostProduct()
        var recipe = Recipe(currentRecipe.recipeName,
            currentRecipe.getTotalCostProduct(),
            currentRecipe.products
        )
        fireBase.collection(auth.currentUser?.email.toString())
            .document("RecipeStore")
            .collection("current")
            .document(recipeName)
            .set(recipe)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    this, "Recipe: $recipeName",
                    Toast.LENGTH_SHORT
                ).show()
                currentRecipe = recipe
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this, "Error adding product: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getRecipeInfo() {
        fireBase.collection(auth.currentUser?.email.toString())
            .document("RecipeStore")
            .collection("current")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if(recipeName == document.data["recipeName"].toString()){
                        var array = document.data["products"] as ArrayList<*>
                        for(i in array.indices) {
                            var p = array[i] as HashMap<*, *>
                            productList.add(Product(
                                p["productName"].toString(),
                                p["productWeight"].toString().toFloat(),
                                p["productPrice"].toString().toFloat(),
                            ))
                        }
                        currentRecipe = Recipe(document.data["recipeName"].toString(),
                            document.data["cost"].toString().toFloat(), productList)
                    }
                }
                totalWeightInfo.text = currentRecipe.getTotalWeightProduct().toString()
                //val cost: String = "%.2f".format(currentRecipe.getTotalCostProduct())
                totalPriceInfo.text = currentRecipe.getTotalCostProduct().toString()

                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting product.", exception)
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