package com.suyash.mockcivilaviationexam.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.model.UserProfile
import kotlinx.coroutines.tasks.await
import java.time.format.DateTimeFormatter

class FirestoreRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore
    
    suspend fun syncFlightEntry(userId: String, flight: FlightEntry) {
        val flightData = mapOf(
            "id" to flight.id,
            "date" to flight.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            "departureAerodrome" to flight.departureAerodrome,
            "arrivalAerodrome" to flight.arrivalAerodrome,
            "aircraftType" to flight.aircraftType,
            "aircraftModel" to flight.aircraftModel,
            "aircraftRegistration" to flight.aircraftRegistration,
            "totalFlightTime" to flight.totalFlightTime,
            "dayTime" to flight.dayTime,
            "nightTime" to flight.nightTime,
            "picTime" to flight.picTime,
            "dualTime" to flight.dualTime,
            "coPilotTime" to flight.coPilotTime,
            "instructorTime" to flight.instructorTime,
            "ifrTime" to flight.ifrTime,
            "vfrTime" to flight.vfrTime,
            "crossCountryTime" to flight.crossCountryTime,
            "simulatorTime" to flight.simulatorTime,
            "isSimulator" to flight.isSimulator,
            "exerciseNumber" to flight.exerciseNumber,
            "lessonNumber" to flight.lessonNumber,
            "remarks" to flight.remarks,
            "instructorName" to flight.instructorName,
            "instructorLicenseNumber" to flight.instructorLicenseNumber,
            "instructorSignature" to flight.instructorSignature,
            "isEndorsed" to flight.isEndorsed,
            "endorsementTimestamp" to flight.endorsementTimestamp,
            "userId" to flight.userId,
            "createdAt" to flight.createdAt,
            "lastModified" to flight.lastModified
        )
        
        firestore.collection("flight_entries")
            .document("${userId}_${flight.id}")
            .set(flightData)
            .await()
    }
    
    suspend fun syncUserProfile(userProfile: UserProfile) {
        val profileData = mapOf(
            "id" to userProfile.id,
            "name" to userProfile.name,
            "licenseNumber" to userProfile.licenseNumber,
            "certificateType" to userProfile.certificateType?.name,
            "email" to userProfile.email,
            "phone" to userProfile.phone,
            "dateOfBirth" to userProfile.dateOfBirth?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            "address" to userProfile.address,
            "medicalExpiry" to userProfile.medicalExpiry?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            "bifrExpiry" to userProfile.bifrExpiry?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            "totalHours" to userProfile.totalHours,
            "picHours" to userProfile.picHours,
            "crossCountryHours" to userProfile.crossCountryHours,
            "nightHours" to userProfile.nightHours,
            "ifrHours" to userProfile.ifrHours,
            "createdAt" to userProfile.createdAt,
            "lastModified" to userProfile.lastModified
        )
        
        firestore.collection("user_profiles")
            .document(userProfile.id)
            .set(profileData)
            .await()
    }
    
    suspend fun getFlightEntries(userId: String): List<Map<String, Any>> {
        val snapshot = firestore.collection("flight_entries")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            doc.data
        }
    }
    
    suspend fun getUserLogbookStats(userId: String): Map<String, Any> {
        val snapshot = firestore.collection("flight_entries")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        
        var totalTime = 0.0
        var totalFlights = 0
        var picTime = 0.0
        var nightTime = 0.0
        var crossCountryTime = 0.0
        var ifrTime = 0.0
        var simulatorTime = 0.0
        
        snapshot.documents.forEach { doc ->
            val data = doc.data
            if (data != null) {
                totalTime += (data["totalFlightTime"] as? Number)?.toDouble() ?: 0.0
                picTime += (data["picTime"] as? Number)?.toDouble() ?: 0.0
                nightTime += (data["nightTime"] as? Number)?.toDouble() ?: 0.0
                crossCountryTime += (data["crossCountryTime"] as? Number)?.toDouble() ?: 0.0
                ifrTime += (data["ifrTime"] as? Number)?.toDouble() ?: 0.0
                simulatorTime += (data["simulatorTime"] as? Number)?.toDouble() ?: 0.0
                totalFlights++
            }
        }
        
        return mapOf(
            "totalTime" to totalTime,
            "totalFlights" to totalFlights,
            "picTime" to picTime,
            "nightTime" to nightTime,
            "crossCountryTime" to crossCountryTime,
            "ifrTime" to ifrTime,
            "simulatorTime" to simulatorTime
        )
    }
    
    suspend fun deleteFlightEntry(userId: String, flightId: Long) {
        firestore.collection("flight_entries")
            .document("${userId}_${flightId}")
            .delete()
            .await()
    }
}