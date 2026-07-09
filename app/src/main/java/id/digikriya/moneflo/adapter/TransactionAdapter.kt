package id.digikriya.moneflo.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.digikriya.moneflo.R
import id.digikriya.moneflo.helper.CategoryVisuals
import id.digikriya.moneflo.model.Cashflow
import java.text.SimpleDateFormat
import java.util.Locale

sealed class HistoryListItem {
    data class Header(val label: String) : HistoryListItem()
    data class Item(val cashflow: Cashflow) : HistoryListItem()
}

class TransactionAdapter(
    private val context: Context,
    private var items: List<HistoryListItem>,
    private val onItemClick: (Cashflow) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view as TextView
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama      : TextView = view.findViewById(R.id.tv_nama_transaksi)
        val tvKategori  : TextView = view.findViewById(R.id.tv_kategori)
        val tvNominal   : TextView = view.findViewById(R.id.tv_nominal)
        val tvTanggal   : TextView = view.findViewById(R.id.tv_tanggal)
        val ivIcon      : ImageView = view.findViewById(R.id.iv_kategori_icon)
        val viewIconBg  : View = view.findViewById(R.id.view_icon_bg)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryListItem.Header -> TYPE_HEADER
            is HistoryListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_date_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val listItem = items[position]) {
            is HistoryListItem.Header -> {
                (holder as HeaderViewHolder).tvLabel.text = listItem.label
            }
            is HistoryListItem.Item -> {
                bindItem(holder as ItemViewHolder, listItem.cashflow)
            }
        }
    }

    private fun bindItem(holder: ItemViewHolder, cashflow: Cashflow) {
        holder.tvNama.text    = cashflow.namaTransaksi
        holder.tvKategori.text = cashflow.namaKategori
        holder.ivIcon.setImageResource(CategoryVisuals.iconFor(cashflow.namaKategori))
        holder.viewIconBg.backgroundTintList = ColorStateList.valueOf(CategoryVisuals.colorFor(cashflow.namaKategori))

        val isIncome = cashflow.jenisTransaksi == "income"
        val prefix   = if (isIncome) "+" else "-"
        holder.tvNominal.text = "$prefix${formatRupiah(cashflow.nominal)}"
        holder.tvNominal.setTextColor(
            context.getColor(if (isIncome) R.color.income_green else R.color.expense_red)
        )

        holder.tvTanggal.text = try {
            val sdf    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date   = sdf.parse(cashflow.timestamp)
            val outSdf = SimpleDateFormat("HH:mm", Locale("id"))
            if (date != null) outSdf.format(date) else cashflow.timestamp
        } catch (e: Exception) {
            cashflow.timestamp
        }

        holder.itemView.setOnClickListener { onItemClick(cashflow) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<HistoryListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatRupiah(amount: Double): String {
        return "Rp${String.format("%,.0f", amount).replace(",", ".")}"
    }
}
