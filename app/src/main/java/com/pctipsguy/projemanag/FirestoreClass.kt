package com.pctipsguy.projemanag

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.userRegisteredSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error writing document",
                            e
                    )
                }
    }

    fun registerBoard(activity: CreateBoardActivity,boardInfo:Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(boardInfo,SetOptions.merge())
            .addOnSuccessListener {
                activity.boardCreationSuccess()
            }
            .addOnFailureListener{e ->
                Log.e(activity.javaClass.simpleName, "registerBoard: ",e )

            }
    }
    fun updateUserProfile(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Data updated successfully!")

                // Notify the success result.

                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun signInUser(activity: Activity, readBoards:Boolean=false) {
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    Log.e(
                            activity.javaClass.simpleName, document.toString()
                    )
                    val loggedInUser = document.toObject(User::class.java)!!
                    when(activity){
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavUserDetails(loggedInUser,readBoards)
                        }
                        is MyProfileActivity ->{
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    when(activity){
                        is SignInActivity ->{
                            activity.hideProgressDialog()
                        }
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while getting loggedIn user details",
                            e
                    )
                }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserID())
            .get()
            .addOnSuccessListener {
                val boardList:ArrayList<Board> = ArrayList()
                for(i in it.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                    mFireStore.collection(Constants.BOARDS)
                        .document(board.documentId)
                        .set(board,SetOptions.merge())
                }
                Log.e("boards","$boardList")
                activity.popBoardsinUI(boardList)
                activity.hideProgressDialog()
            }
            .addOnFailureListener{
                activity.hideProgressDialog()
                Log.e("boarderror", "getBoardsList: ${it.printStackTrace()}")
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                activity.boardDetails(document.toObject(Board::class.java)!!)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun addUpdateTaskList(activity: TaskListActivity, board: Board) {

        Log.e("tessst", board.documentId)
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {

        mFireStore.collection(Constants.USERS) // Collection Name
            .whereIn(Constants.ID, assignedTo) // Here the database field name and the id's of the members.
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if(activity is MembersActivity)
                    activity.setupMembersList(usersList)
                if(activity is TaskListActivity)
                    activity.boardMembersDetailList(usersList)
            }
            .addOnFailureListener { e ->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {

        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }else if(activity is CardDetailsActivity){
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

}