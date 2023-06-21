package com.pctipsguy.projemanag

import android.os.Parcel
import android.os.Parcelable

data class Board(
    val boardName: String = "",
    val boardImage: String = "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    var documentId: String = "",
    var taskList: ArrayList<Task> = ArrayList()
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.createStringArrayList()!!,
        source.readString()!!,
        source.createTypedArrayList(Task.CREATOR)!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(boardName)
        writeString(boardImage)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeString(documentId)
        writeTypedList(taskList)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Board> = object : Parcelable.Creator<Board> {
            override fun createFromParcel(source: Parcel): Board = Board(source)
            override fun newArray(size: Int): Array<Board?> = arrayOfNulls(size)
        }
    }
}