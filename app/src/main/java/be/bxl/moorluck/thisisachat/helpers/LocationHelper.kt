package be.bxl.moorluck.thisisachat.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*

class LocationHelper(val activity: AppCompatActivity, val onLocationEventListener: (Coordinate) -> Unit) {

    // Définition d'un classe pour stocker le resultat du GPS
    data class Coordinate(val lat: Double, val lon: Double)

    companion object {
        const val REQUEST_PERMSSION_GPS: Int = 2_423_251
    }



    /**
     * Méthode pour obtenir la derniere position GPS connu
     * -> Le résultat sera envoyé sous forme d'un event via un lamdba
     */
    fun getLastLocation() {
        if(!checkPermission()) {
            requestPermission()
            return  // Fin prématuré -> Permission manquante !
        }

        if(!isLocationEnable()) {
            requestOpenParameter()
            return  // Fin prématuré -> Le systeme GPS n'est pas activé
        }

        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)

        fusedLocationClient.lastLocation.addOnCompleteListener(activity) {
            // La "task" contient un resultat qui contient la location
            // -> Ce resultat peut être null, dans se cas, il faut faire un demander de localisation
            val location: Location? = it.result

            if(location != null) {
                // Si les données sont OK, on envoie les données via un event
                val coord: Coordinate = Coordinate(location.latitude, location.longitude)
                onLocationEventListener.invoke(coord)
            }
            else {
                requestLocationData();
            }
        }
    }

    /**
     * Méthode pour savoir si la location de l'appareil est activé
     * @return Booléen avec le resultat
     */
    fun isLocationEnable() : Boolean {
        val locationManager: LocationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestOpenParameter() {
        val builder : AlertDialog.Builder = AlertDialog.Builder(activity).apply {
            setTitle("Veuillez activé le GPS")
            setCancelable(false)
            setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                // Ouverture de la fenetre de parametre GPS du smartphone
                val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            })
            setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialog, which ->  })
        }
        builder.show()
    }


    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        // Configuration de la demande
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0            // Zero autorisé, mais pas conseillé (en cas d'optimisation de la demande)
            fastestInterval = 0     // -> Dans notre cas, on se limite à une seul requete
            numUpdates = 1
        }

        // Envoie de la demande de location
        val fusedLocationClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val coord: Coordinate = Coordinate(
                result.lastLocation.latitude,
                result.lastLocation.longitude
            )

            this@LocationHelper.onLocationEventListener(coord)
        }
    }

    private fun checkPermission() : Boolean {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        activity.requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMSSION_GPS
        )
    }
}