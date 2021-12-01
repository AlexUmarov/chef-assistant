package ru.uao.chef.assistant.ui.home

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.databinding.FragmentHomeBinding
import ru.uao.chef.assistant.ui.home.data.Recipe
import ru.uao.chef.assistant.ui.home.view.RecipeListAdapter

class HomeFragment : Fragment(), RecipeListAdapter.OnItemClickListener {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var fireBase = Firebase.firestore


    private lateinit var rvCartList: RecyclerView
    private lateinit var addingCartBtn: FloatingActionButton
    private lateinit var recipeList: ArrayList<Recipe>
    private lateinit var recipeNewList: ArrayList<Recipe>
    private lateinit var recipeDeleteList: ArrayList<Recipe>
    private lateinit var recipeListAdapter: RecipeListAdapter
    private lateinit var ctx: Context


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        ctx = root.context

        FirebaseApp.initializeApp(root.context)
        auth = FirebaseAuth.getInstance()
        rvCartList = binding.recyclerCartList
        addingCartBtn = binding.addingCartBtn
        recipeList = ArrayList()
        recipeNewList = ArrayList()
        recipeDeleteList = ArrayList()
        recipeListAdapter = RecipeListAdapter(root.context, recipeList, recipeDeleteList, this)

        rvCartList.layoutManager = LinearLayoutManager(root.context)
        rvCartList.adapter = recipeListAdapter
        getRecipeInfo()

        addingCartBtn.setOnClickListener {
            addRecipe()
        }

        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getRecipeInfo() {
        fireBase.collection(auth.currentUser?.email.toString())
            .document("RecipeStore")
            .collection("current")
            .get()
            .addOnSuccessListener { result ->
                recipeList.clear()
                for (document in result) {
                    val r = Recipe(document.data["recipeName"].toString())
                    recipeList.add(r)
                }
                recipeListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting product.", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(
            context, "Add product: ${recipeList[position].recipeName}",
            Toast.LENGTH_SHORT
        ).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addRecipe() {
        val inflter = LayoutInflater.from(context)
        val v = inflter.inflate(R.layout.add_recipe_item, null)
        val recipeName = v.findViewById<EditText>(R.id.recipeName)


        val addDialog = AlertDialog.Builder(ctx)

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok") { dialog, _ ->
            when {
                "" == recipeName.text.toString() -> {
                    Toast.makeText(context, "Set param recipe Name", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val recipeName = recipeName.text.toString()
                    val recipe = Recipe(recipeName)
                    recipeNewList.add(recipe)
                    recipeList.add(recipe)
                    recipeListAdapter.notifyDataSetChanged()
                    saveProductInfo()
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

    private fun saveProductInfo() {
        recipeDeleteList.forEach {
            fireBase.collection(auth.currentUser?.email.toString())
                .document("RecipeStore")
                .collection("current")
                .document(it.recipeName)
                .delete()
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        context, "Document ${it.recipeName} successfully deleted!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { documentReference ->
                    Toast.makeText(
                        context, "Error deleting document ${it.recipeName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        recipeNewList.forEach {
            fireBase.collection(auth.currentUser?.email.toString())
                .document("RecipeStore")
                .collection("current")
                .document(it.recipeName)
                .set(it)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        context, "Add product: ${it.recipeName}",
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
        recipeDeleteList.clear()
        recipeNewList.clear()
    }
}