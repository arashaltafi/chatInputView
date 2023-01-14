package com.arash.altafi.chatinputview.ext

import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.provider.ContactsContract
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.arash.altafi.chatinputview.databinding.LayoutToastBinding
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder


fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Context.inflateView(@LayoutRes layoutRes: Int): View {
    return LayoutInflater.from(this)
        .inflate(layoutRes, null)
}

fun Context.makeDialog(
    @LayoutRes layoutRes: Int,
    @StyleRes themRes: Int,
    isBottom: Boolean = false
): AppCompatDialog {
    return AppCompatDialog(this, themRes)
        .apply {
            setContentView(inflateView(layoutRes))
            if (isBottom) {
                val wManager: WindowManager.LayoutParams = this.window?.attributes!!
                wManager.gravity = Gravity.BOTTOM or Gravity.CENTER
            }
        }
}

fun Context.openGoogleMap(lat: String, lng: String) {
    try {
        val strUri = "http://maps.google.com/maps?q=loc:$lat,$lng"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        ContextCompat.startActivity(this, intent, null)
    } catch (e: ActivityNotFoundException) {
        "${e.message}".logE("openGoogleMap")
    }
}

fun Context.openMap(lat: String, lng: String) {
    val intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:$lat,$lng"))
    ContextCompat.startActivity(this, intent, null)
}

fun Context.openAppInfoSetting() {
    //redirect user to app Settings
    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    i.addCategory(Intent.CATEGORY_DEFAULT)
    i.data = Uri.parse("package:$packageName")
    ContextCompat.startActivity(this, i, null)
}

fun Context.openURL(url: String) {
    try {
        val fullUrl = if (url.startsWith("http")) url else "http://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toast("browser_not_found")
    }
}

fun Context.openDownloadURL(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    when {
        this.isInstalled("com.android.chrome") -> intent.setPackage("com.android.chrome")
        this.isInstalled("org.mozilla.firefox") -> intent.setPackage("org.mozilla.firefox")
        this.isInstalled("com.opera.mini.android") -> intent.setPackage("com.opera.mini.android")
        this.isInstalled("com.opera.mini.android.Browser") -> intent.setPackage("com.opera.mini.android.Browser")
        else -> this.openURL(url)
    }
    startActivity(intent)
}

private fun Context.isInstalled(packageName: String): Boolean {
    return try {
        this.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.openFile(uri: Uri?, fileMimType: String?) {
    try {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, fileMimType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    } catch (e: Exception) {
        println(e.message)
    }
}

fun Context.share(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Context.shareTextWithImage(
    applicationId: String,
    bitmap: Bitmap,
    body: String,
    title: String,
    subject: String
) {
    val file = File(externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
    val out = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    out.close()
    val bmpUri = if (Build.VERSION.SDK_INT < 24) {
        Uri.fromFile(file)
    } else {
        FileProvider.getUriForFile(
            this, "$applicationId.fileprovider", file
        )
    }

    val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_TEXT, title + "\n\n" + body)
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_STREAM, bmpUri)
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share News")
    startActivity(shareIntent)
}

fun Context.openCall(phoneNumber: String) {
    ContextCompat.startActivity(
        this,
        Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", phoneNumber, null)
        ),
        null
    )
}

fun Context.openSMS(mobile: String, body: String = "") {
    val smsIntent = Intent(Intent.ACTION_VIEW)
    smsIntent.data = Uri.parse("sms:$mobile")
    smsIntent.putExtra("sms_body", body)
    ContextCompat.startActivity(this, smsIntent, null)
}

fun Context.shareApp() {
    val app: ApplicationInfo = applicationContext.applicationInfo
    val filePath: String = app.sourceDir
    val intent = Intent(Intent.ACTION_SEND)

    // MIME of .apk is "application/vnd.android.package-archive".
    // but Bluetooth does not accept this. Let's use "*/*" instead.
    intent.type = "*/*"


    // Append file and send Intent
    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(filePath)))
    startActivity(Intent.createChooser(intent, "share_app"))
}

fun Context.addContactIntent(): Intent {
    return Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
        type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
    }
}

fun Context.addAsContactConfirmed(
    displayName: String,
    mobileNumber: String,
    workNumber: String,
    photo: Bitmap? = null
) {
    val intent = Intent(Intent.ACTION_INSERT)
    intent.type = ContactsContract.Contacts.CONTENT_TYPE
    intent.putExtra(ContactsContract.Intents.Insert.NAME, displayName)
    intent.putExtra(ContactsContract.Intents.Insert.PHONE, mobileNumber)
    intent.putExtra(
        ContactsContract.Intents.Insert.PHONE_TYPE,
        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
    )
    intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, workNumber)
    intent.putExtra(
        ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
        ContactsContract.CommonDataKinds.Phone.TYPE_WORK
    )

    val data: ArrayList<ContentValues> = ArrayList<ContentValues>()

    val row = ContentValues()
    row.put(
        ContactsContract.Data.MIMETYPE,
        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
    )
    row.put(ContactsContract.CommonDataKinds.Photo.PHOTO, photo?.toByteArray())
    data.add(row)
    intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data)
//    intent.putExtra(ContactsContract.CommonDataKinds.Photo.PHOTO, photo?.toByteArray())

    startActivity(intent)
}

fun Context.openEmail(
    addresses: Array<String>,
    cc: Array<String> = emptyArray(),
    bcc: Array<String> = emptyArray(),
    subject: String? = null,
    message: String? = null
) {
    //https://developer.android.com/guide/components/intents-common#ComposeEmail
    val intentGoogle = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, addresses)
        putExtra(Intent.EXTRA_CC, cc)
        putExtra(Intent.EXTRA_BCC, bcc)
        putExtra(Intent.EXTRA_SUBJECT, subject ?: "")
        putExtra(Intent.EXTRA_TEXT, message ?: "")
    }

    if (intentGoogle.resolveActivity(packageManager) != null)
        startActivity(intentGoogle)
    else {
        toast("email_not_found")
    }

}

@ColorInt
fun Context.getAttrColor(@AttrRes attrID: Int): Int {
    val typedValue = TypedValue()
    val theme = this.theme
    theme.resolveAttribute(attrID, typedValue, true)
    return typedValue.data
}

fun Context.getAttr(attrID: Int): Int {
    val typedValue = TypedValue()
    val theme = this.theme
    theme.resolveAttribute(attrID, typedValue, true)
    return typedValue.data
}

fun Context.getDrawableCompat(res: Int): VectorDrawableCompat? {
    return VectorDrawableCompat.create(resources, res, theme)
}

fun Context.hasPermissions(vararg permission: String): Boolean {
    var result = true

    permission.forEach {
        val a = ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        result = result && a
    }

    return result
}


fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.getStatusBarHeightPixel(): Int = try {
    val res = resources.getIdentifier("status_bar_height", "dimen", "android")

    resources.getDimensionPixelSize(res)
} catch (e: Exception) {
    "Context.getStatusBarHeightPixel: error: $e".logE("getStatusBarHeightPixel")

    80
}

fun Context.getActionBarHeightPixel(): Int {
    val styledAttributes: TypedArray =
        theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
    val mActionBarSize = styledAttributes.getDimension(0, 157f).toInt()
    styledAttributes.recycle()

    return mActionBarSize
}

fun Context.copyToClipboard(text: String) {
    val myClipboard: ClipboardManager? =
        ContextCompat.getSystemService(this, ClipboardManager::class.java)

    val myClip = ClipData.newPlainText("copied:", text)
    myClipboard!!.setPrimaryClip(myClip)
}


/**
 * only use it when application is in foreground
 *
 * @param theView: to show up of the view
 */
fun Context.toastCustom(text: String, @DrawableRes icon: Int? = null, theView: View? = null) {
    val viewBinding = LayoutToastBinding.inflate(LayoutInflater.from(this)).apply {
        tv.text = text
        icon?.let {
            tv.setDrawable(icon, 0, 0, 0)
        }
    }

    Toast(this).apply {

        theView?.let {
            val absoluteLocation = IntArray(2)
            theView.getLocationInWindow(absoluteLocation)
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 200)
        } ?: run {
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 200)
        }

        view = viewBinding.root
        duration = Toast.LENGTH_SHORT
        show()
    }
}


fun Fragment.toastCustom(text: String, @DrawableRes icon: Int? = null, view: View? = null) =
    requireContext().toastCustom(text, icon, view)