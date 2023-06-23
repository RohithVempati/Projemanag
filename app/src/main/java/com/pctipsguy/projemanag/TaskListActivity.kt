package com.pctipsguy.projemanag

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.pctipsguy.projemanag.databinding.ActivityTaskListBinding

class TaskListActivity : BaseActivity(){
    private var binding:ActivityTaskListBinding?=null
    private lateinit var mBoardDocumentId: String
    private lateinit var mBoardDetails: Board
    private var mTaskList:ArrayList<Task> = ArrayList()
    lateinit var mAssignedMemberDetailList:ArrayList<User>

    private val boardDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
    }

    private fun setupActionBar() {

        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.boardName
        }
        binding?.toolbarTaskListActivity?.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    fun boardDetails(board: Board) {
        mBoardDetails = board
        hideProgressDialog()
        setupActionBar()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    fun createTaskList(taskListName: String) {
        Log.e("Task List Name", taskListName)
        val task = Task(taskListName, FirestoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0, task)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        //mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())
        val card = Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)
        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)
        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )
        mBoardDetails.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailList)
        boardDetailsLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                intent = Intent(this@TaskListActivity, MembersActivity::class.java)
                    .putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                boardDetailsLauncher.launch(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun boardMembersDetailList(list:ArrayList<User>){
        mAssignedMemberDetailList = list
        hideProgressDialog()
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)
        mTaskList = mBoardDetails.taskList
        if(mBoardDetails.taskList.size>2)
            mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        binding?.rvTaskList?.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)
        val adapter = TaskListItemsAdapter(this@TaskListActivity, mTaskList)
        binding?.rvTaskList?.adapter = adapter
    }
}