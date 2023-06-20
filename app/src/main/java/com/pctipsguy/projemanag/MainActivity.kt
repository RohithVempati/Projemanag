package com.pctipsguy.projemanag

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.pctipsguy.projemanag.databinding.ActivityMainBinding
import com.pctipsguy.projemanag.databinding.AppBarMainBinding
import com.pctipsguy.projemanag.databinding.ContentMainBinding
import com.pctipsguy.projemanag.databinding.NavHeaderMainBinding

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener{

    private var binding:ActivityMainBinding? = null
    private var appBarBinding:AppBarMainBinding? = null
    private var navbinding:NavHeaderMainBinding? = null
    private var contentBinding:ContentMainBinding?=null

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirestoreClass().signInUser(this)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        appBarBinding = AppBarMainBinding.bind(binding?.root!!)
        appBarBinding?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        appBarBinding?.toolbarMainActivity?.title = "Welcome"
        setSupportActionBar(appBarBinding?.toolbarMainActivity)
        appBarBinding?.toolbarMainActivity?.setNavigationOnClickListener {
        toggleDrawer()
        }
        binding?.navView?.setNavigationItemSelectedListener(this)
        contentBinding = ContentMainBinding.bind(binding?.root!!)
        contentBinding?.fabCreateBoard?.setOnClickListener {
            startActivity(Intent(this,CreateBoardActivity::class.java))
        }
        FirestoreClass().signInUser(this)
    }
    private fun toggleDrawer(){
        if(binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    fun updateNavUserDetails(user:User){
        navbinding = NavHeaderMainBinding.bind(binding?.navView!!.getHeaderView(0))
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navbinding?.ivUserImage!!)
        navbinding?.tvUsername?.text = user.name
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                resultLauncher.launch(intent)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}