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
    fun updateUserProfile(activity: MyProfileActivity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile dataa updated")
                activity.profileUpdateSuccess()
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

}