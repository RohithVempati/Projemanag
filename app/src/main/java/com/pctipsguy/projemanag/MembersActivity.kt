package com.pctipsguy.projemanag

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.pctipsguy.projemanag.databinding.ActivityMembersBinding
import com.pctipsguy.projemanag.databinding.DialogSearchMemberBinding

class MembersActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var binding:ActivityMembersBinding? = null
    private lateinit var mAssignedMembersList:ArrayList<User>
    private var anyChangesDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        setupActionBar()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(
            this@MembersActivity,
            mBoardDetails.assignedTo
        )
    }

    override fun onBackPressed() {
        if (anyChangesDone) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private fun setupActionBar() {

        setSupportActionBar(binding?.toolbarMembersActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            val title = "Members List"
            actionBar.title = title
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding?.toolbarMembersActivity?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun setupMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()
        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this@MembersActivity)
        binding?.rvMembersList?.setHasFixedSize(true)
        val adapter = MemberListItemsAdapter(this@MembersActivity, list)
        binding?.rvMembersList?.adapter = adapter
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        val dialogSearchMemberBinding:DialogSearchMemberBinding =
            DialogSearchMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogSearchMemberBinding.root)
        dialogSearchMemberBinding.tvAdd.setOnClickListener(View.OnClickListener {

            val email = dialogSearchMemberBinding.etEmailSearchMember.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this@MembersActivity, email)
            } else {
                showErrorSnackBar("Please enter members email address.")
            }
        })
        dialogSearchMemberBinding.tvCancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        dialog.show()
    }

    fun memberDetails(user: User) {
        if(mBoardDetails.assignedTo.contains(user.id)){
            hideProgressDialog()
            showErrorSnackBar("Member already in Board")
        }
        else {
            mBoardDetails.assignedTo.add(user.id)
            FirestoreClass().assignMemberToBoard(this@MembersActivity, mBoardDetails, user)
        }
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesDone = true
        setupMembersList(mAssignedMembersList)
    }
}