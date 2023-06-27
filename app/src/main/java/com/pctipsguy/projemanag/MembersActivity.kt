package com.pctipsguy.projemanag

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pctipsguy.projemanag.databinding.ActivityMembersBinding
import com.pctipsguy.projemanag.databinding.DialogSearchMemberBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

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
        lifecycleScope.launch(Dispatchers.IO) {SendNotificationToUserAsyncTask(mBoardDetails.boardName, user.fcmToken).send()}
    }

    private inner class SendNotificationToUserAsyncTask(val boardName: String,val token: String){

            fun send():String{
                runOnUiThread{showProgressDialog(resources.getString(R.string.please_wait))}
                var result: String
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(Constants.FCM_BASE_URL)
                    connection = url.openConnection() as HttpURLConnection
                    connection.doOutput = true
                    connection.doInput = true
                    connection.instanceFollowRedirects = false
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("charset", "utf-8")
                    connection.setRequestProperty("Accept", "application/json")
                    connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                    )
                    connection.useCaches = false
                    val wr = DataOutputStream(connection.outputStream)
                    val jsonRequest = JSONObject()
                    val dataObject = JSONObject()
                    dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")
                    dataObject.put(
                        Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mAssignedMembersList[0].name}"
                    )
                    jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                    jsonRequest.put(Constants.FCM_KEY_TO, token)
                    wr.writeBytes(jsonRequest.toString())
                    wr.flush()
                    wr.close()
                    val httpResult: Int = connection.responseCode
                    if (httpResult == HttpURLConnection.HTTP_OK) {

                        val inputStream = connection.inputStream

                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val sb = StringBuilder()
                        var line: String?
                        try {
                            while (reader.readLine().also { line = it } != null) {
                                sb.append(line + "\n")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            try {
                            inputStream.close()
                            } catch (e: IOException) {
                            e.printStackTrace()
                            }
                        }
                        result = sb.toString()
                    } else {
                        result = connection.responseMessage
                    }
                } catch (e: SocketTimeoutException) {
                    result = "Connection Timeout"
                } catch (e: Exception) {
                    result = e.stackTraceToString()
                } finally {
                    connection?.disconnect()
                }
                runOnUiThread{ hideProgressDialog() }
                Log.i("JSON RESPONSE RESULT", result)
                return result
            }

    }

}