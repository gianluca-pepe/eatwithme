package com.brugia.eatwithme.myprofile

import android.app.Application
import androidx.lifecycle.*
import com.brugia.eatwithme.data.Person
import com.brugia.eatwithme.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth

class MyProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val mUser = FirebaseAuth.getInstance().currentUser
    private val personID: String = mUser?.uid.toString()

    private val userRepository: UserRepository = UserRepository(application)

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
        myprofileLiveData = userRepository.currentPersonLiveData
        userRepository.getCurrentUser()
    }

    private val url = "https://sapienzaengineering.eu.pythonanywhere.com/api/v1.0/users"

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
                    application = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
