package com.teamkita.paskita.ui.bottomnavigation.user.cekproduk

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.FragmentCekProdukBinding
import com.teamkita.paskita.ml.ModelUnquant
import com.teamkita.paskita.ui.bottomnavigation.penjual.DaftarPenjual
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.ProdukAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangViewModel
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.DaftarPesanActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class CekProdukFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentCekProdukBinding? = null

    private val PERMISSION_CAMERA_REQUEST_CODE = 1
    private val PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1

    private lateinit var listProduk: ArrayList<Produk>
    private val db = Firebase.firestore
    private val binding get() = _binding!!

    private val imageSize = 224;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCekProdukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProduk.layoutManager = LinearLayoutManager(requireContext())
        listProduk = arrayListOf()
        cekPermission()

        setupAction()
        playAnimation()

    }

    private fun setupAction() {

        binding.ivPesan.setOnClickListener {
            val intent = Intent(requireContext(), DaftarPesanActivity::class.java)
            startActivity(intent)

            activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.ivNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifikasiActivity::class.java)
            startActivity(intent)

            activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.ivKeranjang.setOnClickListener {
            val intent = Intent(requireContext(), KeranjangActivity::class.java)
            startActivity(intent)
        }

        binding.cvFotoProduk.setOnClickListener {
            pilihGambarProduk()
        }
    }

    private fun searchProduk(searchQuery: String?) {
        searchQuery?.let { query ->
            listProduk.clear()
            val produkCollection = db.collection("produk")

            produkCollection.get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        for (data in snapshot.documents) {
                            val produk: Produk? = data.toObject(Produk::class.java)
                            val namaProduk = produk?.nama_produk
                            
                            if (!namaProduk.isNullOrBlank() && query.isNotEmpty() &&
                                namaProduk!!.contains(query, ignoreCase = true)
                            ) {
                                produk?.let { listProduk.add(it) }
                                binding.tvProdukSerupa.visibility = View.VISIBLE
                                binding.rvProduk.visibility = View.VISIBLE
                            }
                        }
                        binding.rvProduk.adapter = ProdukAdapter(listProduk)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            // Handle null searchQuery here if needed
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 20 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val path: Uri? = data.data

            val image = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, path)
            val dimension = min(image.width, image.height)
            val thumbnail = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
            binding.ivProduk.setImageBitmap(thumbnail)
            val scaledImage = Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false)
            classifyImage(scaledImage)
        }

        if (requestCode == 10 && resultCode == AppCompatActivity.RESULT_OK) {

            val bitmap = data?.extras?.get("data") as Bitmap
            val dimension = min(bitmap.width, bitmap.height)
            val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
            binding.ivProduk.setImageBitmap(thumbnail)

            val scaledImage = Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false)
            classifyImage(scaledImage)
        }
    }

    private fun classifyImage(image: Bitmap) {
        try {
            val model = ModelUnquant.newInstance(requireContext())

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

            var pixel = 0
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val value = intValues[pixel++]
                    byteBuffer.putFloat(((value shr 16) and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat(((value shr 8) and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat((value and 0xFF) * (1f / 255f))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val confidences = outputFeature0.floatArray

            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }
            val classes = arrayOf("Bakpia", "Dodol", "Kembang Goyang", "Keripik Singkong", "Kue Lapis", "Lumpia", "Pie Susu")
            val desc = arrayOf(
                "Bakpia adalah kue tradisional dari Yogyakarta, Indonesia, yang terkenal dengan isian kacang hijau atau ubi yang manis. \"Bakpia\" sendiri merupakan kata serapan dari bahasa Hokkien yang artinya \"kue kacang\". Fun fact-nya adalah bahwa bakpia diperkirakan pertama kali dibawa ke Indonesia oleh imigran Tionghoa pada abad ke-20 dan telah menjadi salah satu oleh-oleh khas yang populer bagi para wisatawan yang mengunjungi Yogyakarta."
                , "Dodol adalah makanan manis yang populer di Indonesia dan beberapa negara Asia Tenggara lainnya. Ini adalah perpaduan antara gula, santan, dan tepung beras atau tepung ketan yang dimasak hingga menjadi padat dengan tekstur yang kenyal.\n" +
                        "\n" +
                        "Fun fact tentang dodol adalah proses pembuatannya memakan waktu yang cukup lama dan membutuhkan kesabaran. Proses memasak dodol bisa memakan waktu hingga beberapa jam dengan terus-menerus mengaduk adonan yang panas di atas api. Dodol sering kali dianggap sebagai simbol kebersamaan dan kerjasama, karena dalam proses pembuatannya membutuhkan kerja sama untuk terus mengaduk adonan agar tidak gosong dan merata panasnya. Dodol juga sering dihadirkan dalam berbagai perayaan dan acara penting di Indonesia."
                , "Kembang goyang adalah salah satu jenis camilan tradisional Indonesia yang terbuat dari adonan tepung beras yang digoreng hingga mengembang dan menjadi keriting seperti bunga. Camilan ini biasanya diberi taburan gula halus atau gula pasir setelah digoreng untuk memberikan rasa manis.\n" +
                        "\n" +
                        "Fun fact tentang kembang goyang adalah proses pembuatannya yang memerlukan keahlian khusus untuk menghasilkan bentuk yang cantik dan mengembang sempurna. Biasanya, adonan tepung beras diletakkan di dalam corong khusus yang bisa digerakkan dengan tangan sehingga adonan dapat dituangkan ke dalam wajan berisi minyak panas dalam gerakan yang membuat adonan membentuk pola seperti bunga yang indah.\n" +
                        "\n" +
                        "Selain itu, kembang goyang sering dianggap sebagai salah satu camilan yang menghadirkan kenangan masa kecil bagi banyak orang Indonesia karena keunikan bentuknya yang menarik dan rasanya yang manis."
                , "Keripik singkong adalah camilan populer di Indonesia yang terbuat dari irisan tipis singkong yang kemudian digoreng hingga kering dan renyah. Singkong dipotong tipis, kadang-kadang dibumbui dengan berbagai rempah atau bumbu sesuai selera, kemudian digoreng hingga menjadi keripik yang garing dan lezat.\n" +
                        "\n" +
                        "Fun fact tentang keripik singkong adalah variasi rasa dan tekstur yang dapat diciptakan. Beberapa produsen keripik singkong menggunakan bumbu pedas, gurih, manis, atau bahkan rasa-rasa unik seperti keju atau barbeque untuk menambah variasi rasa yang menarik bagi konsumen. Keripik singkong juga memiliki nilai nutrisi karena singkong kaya akan karbohidrat, serat, dan beberapa nutrisi lainnya.\n" +
                        "\n" +
                        "Selain menjadi camilan favorit di Indonesia, keripik singkong juga sering dijadikan oleh-oleh yang populer bagi wisatawan yang berkunjung ke daerah-daerah di Indonesia yang terkenal dengan produksi keripik singkongnya."
                , "Kue Lapis adalah salah satu kue tradisional Indonesia yang terdiri dari lapisan-lapisan yang terbuat dari campuran tepung beras, santan, gula, dan pewarna alami. Proses pembuatannya melibatkan menuangkan lapisan demi lapisan adonan ke dalam loyang dan dikukus hingga matang. Biasanya, kue ini memiliki dua lapisan warna, seperti putih dan merah, atau warna-warna lainnya yang menarik.\n" +
                        "\n" +
                        "Fun fact tentang kue lapis adalah bahwa proses pembuatannya membutuhkan ketelitian yang tinggi untuk mendapatkan lapisan yang rapi dan presisi. Selain itu, karena penggunaan bahan alami dalam pewarnaan, beberapa variasi kue lapis menggunakan bahan seperti pandan untuk hijau atau ubi ungu untuk merah muda, menambahkan aroma dan rasa yang khas.\n" +
                        "\n" +
                        "Kue Lapis sering dihadirkan dalam berbagai acara, termasuk perayaan seperti Lebaran atau acara spesial lainnya sebagai simbol keberuntungan dan kesuksesan dalam budaya Indonesia."
                , "Lumpia adalah hidangan yang terkenal di Indonesia, berupa gulungan kulit tipis yang diisi dengan berbagai bahan, seperti rebung, wortel, daging cincang, udang, atau campuran sayuran lainnya. Kemudian lumpia ini digoreng hingga kulitnya menjadi renyah.\n" +
                        "\n" +
                        "Fun fact tentang lumpia adalah variasi yang ada dalam pembuatannya. Ada dua jenis lumpia yang populer di Indonesia: lumpia goreng (yang digoreng) dan lumpia basah (yang tidak digoreng dan sering disajikan dengan saus). Lumpia merupakan contoh makanan yang diadaptasi dari masakan Tionghoa yang kemudian menjadi salah satu makanan yang sangat populer di Indonesia.\n" +
                        "\n" +
                        "Lumpia juga sering disajikan sebagai camilan atau hidangan pembuka dalam berbagai acara, mulai dari acara keluarga hingga acara besar seperti pernikahan. Versatilitasnya dalam isiannya membuatnya disukai oleh banyak orang karena dapat disesuaikan dengan preferensi masing-masing."
                , "Pie susu adalah kue yang terkenal dari Bali, Indonesia. Kue ini terdiri dari kulit pie yang tipis dan renyah yang diisi dengan campuran susu, telur, gula, dan rempah-rempah seperti kayu manis. Isian ini kemudian dipanggang hingga matang.\n" +
                        "\n" +
                        "Fun fact tentang pie susu adalah kelezatannya yang memikat banyak orang dan sering dijadikan oleh-oleh khas dari Bali. Setiap produsen pie susu memiliki resep rahasia mereka sendiri untuk menciptakan rasa yang unik dan istimewa. Selain itu, pie susu juga sering dianggap sebagai simbol dari Bali dan menjadi oleh-oleh favorit bagi wisatawan yang berkunjung ke pulau tersebut.")

            binding.cvHasil.visibility = View.VISIBLE

            binding.tvResult.text = classes[maxPos]
            binding.tvFunFact.text = desc[maxPos]

            val query = binding.tvResult.text.toString()
            searchProduk(query)

            model.close()
        } catch (e: IOException) {

        }
    }

    private fun pilihGambarProduk() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Pilih Gambar")
        builder.setIcon(R.drawable.tulisan_paskita)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Ambil Foto" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 10)
                }
                items[item] == "Ambil Dari Galeri" -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 20)
                }
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun cekPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA_REQUEST_CODE
            )
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(binding.tvMessage, View.ALPHA, 1f).setDuration(100)
        val cvFotoProduk =
            ObjectAnimator.ofFloat(binding.cvFotoProduk, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                cvFotoProduk
            )
            startDelay = 100
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CekProdukFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}