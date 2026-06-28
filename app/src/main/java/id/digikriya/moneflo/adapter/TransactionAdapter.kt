package id.digikriya.moneflo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.digikriya.moneflo.R
import id.digikriya.moneflo.model.Cashflow
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val context: Context,
    private var items: List<Cashflow>,
    private val onItemClick: (Cashflow) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama      : TextView = view.findViewById(R.id.tv_nama_transaksi)
        val tvKategori  : TextView = view.findViewById(R.id.tv_kategori)
        val tvNominal   : TextView = view.findViewById(R.id.tv_nominal)
        val tvTanggal   : TextView = view.findViewById(R.id.tv_tanggal)
        val tvInitial   : TextView = view.findViewById(R.id.tv_kategori_initial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cashflow = items[position]

        holder.tvNama.text    = cashflow.namaTransaksi
        holder.tvKategori.text = cashflow.namaKategori
        holder.tvInitial.text  = cashflow.namaKategori.firstOrNull()?.uppercase() ?: "?"

        val isIncome = cashflow.jenisTransaksi == "income"
        val prefix   = if (isIncome) "+" else "-"
        holder.tvNominal.text = "$prefix${formatRupiah(cashflow.nominal)}"
        holder.tvNominal.setTextColor(
            context.getColor(if (isIncome) R.color.income_green else R.color.expense_red)
        )

        // Format tanggal
        holder.tvTanggal.text = try {
            val sdf    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date   = sdf.parse(cashflow.timestamp)
            val outSdf = SimpleDateFormat("dd MMM yyyy", Locale("id"))
            if (date != null) outSdf.format(date) else cashflow.timestamp
        } catch (e: Exception) {
            cashflow.timestamp
        }

        holder.itemView.setOnClickListener { onItemClick(cashflow) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Cashflow>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatRupiah(amount: Double): String {
        return "Rp${String.format("%,.0f", amount).replace(",", ".")}"
    }
}
