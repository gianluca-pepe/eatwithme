package com.brugia.eatwithme.myprofile

import android.app.Application
import androidx.lifecycle.*
import com.brugia.eatwithme.data.Person
import com.brugia.eatwithme.data.user.UserRepository
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.google.firebase.auth.FirebaseAuth

class MyProfileViewModel(val userRepository: UserRepository) : ViewModel() {
    private val mUser = FirebaseAuth.getInstance().currentUser
    private val personID: String = mUser?.uid.toString()


    var myprofileLiveData: LiveData<Person> = MutableLiveData(
            Person(
                    id = personID,
                    name = null,
                    surname = null,
                    description = null,
                    email = FirebaseAuth.getInstance().currentUser?.email,
                    birthday = null,
                    profile_pic = null
            )
    )

    init {
        println("ottengo persona")
        myprofileLiveData = userRepository.currentPersonLiveData
        println("persona ottenuta: "+ myprofileLiveData.value)
    }

    fun checkPersonData() {
        userRepository.checkPersonData()
    }

    //Update person data on db
    fun updateCurrentPerson(name: String?, surname: String?, description: String?, birthday: String?){
        userRepository.updateCurrentPerson(name, surname, description, birthday)
    }

    fun updatePersonPic(profile_pic: String?){
        userRepository.updateCurrentPersonPic(profile_pic)
    }

    /*Delete current person data from DB (keep care to logout the person on the end and redirect to the login page..)*/
    fun deleteCurrentPersonData(){
        userRepository.deleteCurrentPersonData()
    }
}

class MyProfileViewModelFactory(private val context: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyProfileViewModel(
                    userRepository = UserRepository.getUserRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
