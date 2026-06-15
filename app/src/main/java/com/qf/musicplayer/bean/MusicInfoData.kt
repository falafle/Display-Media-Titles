package com.qf.musicplayer.bean

import android.os.Parcel
import android.os.Parcelable

data class MusicInfoData(
    var name: String? = null,
    var artist: String? = null,
    var path: String? = null,
    var currTime: Int = 0,
    var totalTime: Int = 0,
    var curPlayStatus: Int = 0,
    var index: Int = 0,
    var listSize: Int = 0,
    var parentFilePath: String? = null,
    var parentFileName: String? = null,
    var favourite: Int = 0,
    var album: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(path)
        parcel.writeInt(currTime)
        parcel.writeInt(totalTime)
        parcel.writeInt(curPlayStatus)
        parcel.writeInt(index)
        parcel.writeInt(listSize)
        parcel.writeString(parentFilePath)
        parcel.writeString(parentFileName)
        parcel.writeInt(favourite)
        parcel.writeString(album)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MusicInfoData> {
        override fun createFromParcel(parcel: Parcel): MusicInfoData {
            return MusicInfoData(parcel)
        }

        override fun newArray(size: Int): Array<MusicInfoData?> {
            return arrayOfNulls(size)
        }
    }
}