'use strict';

// Endpoint disesuaikan dengan CheckoutBoundary yang digunakan backend QuickBite.
const API_BASE = '/api/v1/checkout';
const state = {
  menus: [],
  cart: new Map(),
  search: '',
  loadingMenus: false,
  submittingCheckout: false,
  submittingCancel: false
};

const elementIds = [
  'connectionState', 'connectionText', 'currentDate', 'globalNotice', 'refreshMenusButton',
  'statMenuCount', 'statCartCount', 'statCartHint', 'statStockCount', 'menuCount', 'menuSearch',
  'menuGrid', 'cartItems', 'cartItemTypes', 'cartQuantity', 'cartSubtotal', 'cartTotal',
  'checkoutForm', 'customerId', 'paymentMethod', 'paymentToken', 'checkoutButton', 'receiptCard',
  'receiptOrderId', 'receiptPaymentId', 'receiptTotal', 'receiptOrderStatus',
  'receiptPaymentStatus', 'receiptReference', 'receiptDate', 'printReceiptButton',
  'cancelForm', 'cancelOrderId', 'cancelReason', 'cancelButton'
];
const el = Object.fromEntries(elementIds.map((id) => [id, document.getElementById(id)]));

const currency = new Intl.NumberFormat('id-ID', {
  style: 'currency', currency: 'IDR', maximumFractionDigits: 0
});

function formatMoney(value) {
  return currency.format(Number(value) || 0);
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[char]);
}

function formatDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return new Intl.DateTimeFormat('id-ID', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

function renderCurrentDate() {
  el.currentDate.textContent = new Intl.DateTimeFormat('id-ID', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
  }).format(new Date());
}

function setConnection(status, message) {
  el.connectionState.classList.remove('online', 'offline');
  if (status) el.connectionState.classList.add(status);
  el.connectionText.textContent = message;
}

function showNotice(message, type = 'info') {
  el.globalNotice.className = `notice ${type}`;
  el.globalNotice.textContent = message;
  el.globalNotice.hidden = false;
  el.globalNotice.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function clearNotice() {
  el.globalNotice.hidden = true;
  el.globalNotice.textContent = '';
  el.globalNotice.className = 'notice';
}

async function readResponse(response) {
  const data = await response.json().catch(() => null);
  if (!response.ok || (data && data.success === false)) {
    let message = data?.message || `Permintaan gagal (HTTP ${response.status}).`;
    if (data?.errors && typeof data.errors === 'object') {
      const details = Object.values(data.errors).flat().filter(Boolean).join(' ');
      if (details) message += ` ${details}`;
    }
    throw new Error(message);
  }
  return data;
}

function productVisual(menu) {
  const name = String(menu.namaMenu || '').toLowerCase();
  if (/kopi|coffee|espresso|latte|cappuccino|americano|teh|tea|minum/.test(name)) {
    return { emoji: '☕', label: 'Minuman' };
  }
  if (/croissant|roti|bread|pastry|donat|donut|bakery|bun/.test(name)) {
    return { emoji: '🥐', label: 'Bakery' };
  }
  if (/cake|kue|dessert|pudding|brownie|muffin/.test(name)) {
    return { emoji: '🍰', label: 'Dessert' };
  }
  if (/burger|sandwich|toast/.test(name)) return { emoji: '🍔', label: 'Makanan' };
  if (/nasi|rice|mie|noodle|pasta/.test(name)) return { emoji: '🍜', label: 'Hidangan' };
  return { emoji: '🍽️', label: 'Menu pilihan' };
}

function quantityFor(idMenu) {
  return state.cart.get(idMenu) || 0;
}

function totalCartQuantity() {
  return [...state.cart.values()].reduce((sum, qty) => sum + qty, 0);
}

function renderStats() {
  const totalStock = state.menus.reduce((sum, menu) => sum + Math.max(0, Number(menu.stok) || 0), 0);
  const quantity = totalCartQuantity();
  el.statMenuCount.textContent = String(state.menus.length);
  el.statCartCount.textContent = String(quantity);
  el.statCartHint.textContent = quantity === 0 ? 'Siap menerima pesanan' : `${state.cart.size} jenis produk dipilih`;
  el.statStockCount.textContent = totalStock.toLocaleString('id-ID');
}

async function loadMenus({ quiet = false } = {}) {
  if (state.loadingMenus) return;
  state.loadingMenus = true;
  el.refreshMenusButton.disabled = true;
  if (!state.menus.length) {
    el.menuGrid.innerHTML = '<div class="loading-card"><span class="loader"></span><span>Sedang mengambil daftar menu…</span></div>';
  }
  try {
    const response = await fetch(`${API_BASE}/menus`, { method: 'GET', headers: { Accept: 'application/json' } });
    const payload = await readResponse(response);
    if (!Array.isArray(payload?.data)) throw new Error('Format daftar menu dari server tidak sesuai.');
    state.menus = payload.data;

    // Sesuaikan keranjang dengan stok terkini dari server.
    for (const [idMenu, quantity] of state.cart.entries()) {
      const menu = state.menus.find((item) => item.idMenu === idMenu);
      if (!menu || Number(menu.stok) <= 0) state.cart.delete(idMenu);
      else if (quantity > Number(menu.stok)) state.cart.set(idMenu, Number(menu.stok));
    }

    setConnection('online', 'Terhubung ke server');
    renderMenus();
    renderCart();
    if (!quiet) clearNotice();
  } catch (error) {
    setConnection('offline', 'Server belum dapat diakses');
    el.menuGrid.innerHTML = `
      <div class="empty-menu"><strong>Belum bisa memuat menu</strong>
        <span>Pastikan QuickBite berjalan di port 8080, lalu pilih “Segarkan menu”.</span>
      </div>`;
    if (!quiet) showNotice(error.message || 'Tidak dapat mengambil daftar menu.', 'error');
  } finally {
    state.loadingMenus = false;
    el.refreshMenusButton.disabled = false;
    renderStats();
    renderCart();
  }
}

function renderMenus() {
  const query = state.search.trim().toLocaleLowerCase('id-ID');
  const filtered = state.menus.filter((menu) => String(menu.namaMenu || '').toLocaleLowerCase('id-ID').includes(query)
    || String(menu.idMenu || '').toLocaleLowerCase('id-ID').includes(query));
  el.menuCount.textContent = query ? `${filtered.length} dari ${state.menus.length} menu` : `${state.menus.length} menu`;
  if (!filtered.length) {
    el.menuGrid.innerHTML = `<div class="empty-menu"><strong>${state.menus.length ? 'Menu tidak ditemukan' : 'Belum ada menu tersedia'}</strong><span>${state.menus.length ? 'Coba kata kunci lain.' : 'Data menu akan muncul setelah tersedia di database.'}</span></div>`;
    renderStats();
    return;
  }

  el.menuGrid.innerHTML = filtered.map((menu, index) => {
    const stock = Math.max(0, Number(menu.stok) || 0);
    const quantity = quantityFor(menu.idMenu);
    const visual = productVisual(menu);
    return `
      <article class="menu-card">
        <div class="menu-art tone-${index % 4}" aria-hidden="true">
          <span class="menu-art-emoji">${visual.emoji}</span>
          <span class="menu-art-label">${escapeHtml(visual.label)}</span>
        </div>
        <div class="menu-card-content">
          <div class="menu-card-top">
            <span class="menu-id">${escapeHtml(menu.idMenu)}</span>
            <span class="stock-pill ${stock <= 0 ? 'out-of-stock' : ''}">${stock <= 0 ? 'Stok habis' : `Stok ${stock}`}</span>
          </div>
          <h3>${escapeHtml(menu.namaMenu)}</h3>
          <div class="menu-price">${formatMoney(menu.harga)}</div>
          <div class="menu-bottom">
            <span class="menu-stock-caption">${stock > 0 ? 'Siap disajikan' : 'Tidak tersedia'}</span>
            <div class="quantity-control" aria-label="Jumlah ${escapeHtml(menu.namaMenu)}">
              <button type="button" data-action="decrease" data-id="${escapeHtml(menu.idMenu)}" aria-label="Kurangi ${escapeHtml(menu.namaMenu)}" ${quantity <= 0 ? 'disabled' : ''}>−</button>
              <span class="quantity-value">${quantity}</span>
              <button type="button" data-action="increase" data-id="${escapeHtml(menu.idMenu)}" aria-label="Tambah ${escapeHtml(menu.namaMenu)}" ${stock <= quantity ? 'disabled' : ''}>+</button>
            </div>
          </div>
        </div>
      </article>`;
  }).join('');
  renderStats();
}

function updateQuantity(idMenu, amount) {
  const menu = state.menus.find((item) => item.idMenu === idMenu);
  if (!menu) return;
  const current = quantityFor(idMenu);
  const next = current + amount;
  const stock = Math.max(0, Number(menu.stok) || 0);
  if (next <= 0) state.cart.delete(idMenu);
  else if (next <= stock) state.cart.set(idMenu, next);
  else showNotice(`Stok ${menu.namaMenu} hanya ${stock}.`, 'info');
  renderMenus();
  renderCart();
}

function renderCart() {
  const rows = [];
  let total = 0;
  const quantity = totalCartQuantity();

  for (const [idMenu, qty] of state.cart.entries()) {
    const menu = state.menus.find((item) => item.idMenu === idMenu);
    if (!menu) continue;
    const subtotal = (Number(menu.harga) || 0) * qty;
    const visual = productVisual(menu);
    total += subtotal;
    rows.push(`
      <div class="cart-item">
        <div class="cart-item-main">
          <span class="cart-item-thumb" aria-hidden="true">${visual.emoji}</span>
          <div class="cart-item-info">
            <div class="cart-item-name">${escapeHtml(menu.namaMenu)}</div>
            <div class="cart-item-sub">${formatMoney(menu.harga)} / item</div>
            <div class="quantity-control compact" aria-label="Atur jumlah ${escapeHtml(menu.namaMenu)}">
              <button type="button" data-action="decrease" data-id="${escapeHtml(idMenu)}" aria-label="Kurangi ${escapeHtml(menu.namaMenu)}">−</button>
              <span class="quantity-value">${qty}</span>
              <button type="button" data-action="increase" data-id="${escapeHtml(idMenu)}" aria-label="Tambah ${escapeHtml(menu.namaMenu)}" ${qty >= Number(menu.stok) ? 'disabled' : ''}>+</button>
            </div>
          </div>
        </div>
        <div class="cart-item-end"><span class="cart-subtotal">${formatMoney(subtotal)}</span><button type="button" class="remove-item" data-action="remove" data-id="${escapeHtml(idMenu)}">Hapus</button></div>
      </div>`);
  }

  el.cartItems.innerHTML = rows.length ? rows.join('') : `
    <div class="empty-cart"><span class="empty-cart-art" aria-hidden="true">☕</span>
      <strong>Keranjang masih kosong</strong><p>Produk pilihanmu akan muncul di sini.</p>
    </div>`;
  el.cartItemTypes.textContent = `${state.cart.size} produk`;
  el.cartQuantity.textContent = `${quantity} item`;
  el.cartSubtotal.textContent = formatMoney(total);
  el.cartTotal.textContent = formatMoney(total);
  el.checkoutButton.disabled = state.cart.size === 0 || state.submittingCheckout || state.loadingMenus;
  el.checkoutButton.innerHTML = state.submittingCheckout
    ? '<span>Memproses checkout…</span><span class="button-arrow" aria-hidden="true">…</span>'
    : '<span>Proses checkout</span><span class="button-arrow" aria-hidden="true">→</span>';
  renderStats();
}

function showReceipt(order) {
  el.receiptOrderId.textContent = order.idPesanan || '—';
  el.receiptPaymentId.textContent = order.idPembayaran || '—';
  el.receiptTotal.textContent = formatMoney(order.totalBiaya);
  el.receiptOrderStatus.textContent = order.statusPesanan || '—';
  el.receiptPaymentStatus.textContent = order.statusPembayaran || '—';
  el.receiptReference.textContent = order.kodeReferensi || '—';
  el.receiptDate.textContent = formatDate(order.tanggal);
  el.cancelOrderId.value = order.idPesanan || '';
  el.receiptCard.hidden = false;
}

async function processCheckout(event) {
  event.preventDefault();
  clearNotice();
  if (state.cart.size === 0) {
    showNotice('Pilih minimal satu menu sebelum checkout.', 'error');
    return;
  }

  const requestBody = {
    idPelanggan: el.customerId.value.trim(),
    keranjangData: [...state.cart.entries()].map(([idMenu, jumlah]) => ({ idMenu, jumlah })),
    metodePembayaran: el.paymentMethod.value,
    tokenKartu: el.paymentToken.value.trim()
  };
  if (!requestBody.idPelanggan || !requestBody.metodePembayaran || !requestBody.tokenKartu) {
    showNotice('ID pelanggan, metode pembayaran, dan token simulasi wajib diisi.', 'error');
    return;
  }

  state.submittingCheckout = true;
  renderCart();
  try {
    const response = await fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(requestBody)
    });
    const payload = await readResponse(response);
    if (!payload?.data?.idPesanan) throw new Error('Checkout merespons, tetapi data pesanan tidak ditemukan. Periksa respons backend.');

    showReceipt(payload.data);
    state.cart.clear();
    renderCart();
    showNotice(payload.message || 'Checkout berhasil diproses.', 'success');
    await loadMenus({ quiet: true });
    el.receiptCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  } catch (error) {
    showNotice(error.message || 'Checkout gagal. Periksa server dan input pesanan.', 'error');
  } finally {
    state.submittingCheckout = false;
    renderCart();
  }
}

async function cancelOrder(event) {
  event.preventDefault();
  clearNotice();
  const orderId = el.cancelOrderId.value.trim();
  const alasan = el.cancelReason.value.trim();
  if (!orderId || !alasan) {
    showNotice('ID pesanan dan alasan pembatalan wajib diisi.', 'error');
    return;
  }
  if (!window.confirm(`Batalkan pesanan ${orderId}?`)) return;

  state.submittingCancel = true;
  el.cancelButton.disabled = true;
  el.cancelButton.innerHTML = '<span aria-hidden="true">↻</span> Memproses pembatalan…';
  try {
    const response = await fetch(`${API_BASE}/${encodeURIComponent(orderId)}/batal`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ alasan })
    });
    const payload = await readResponse(response);
    showNotice(payload?.message || `Pesanan ${orderId} berhasil dibatalkan.`, 'success');

    if (el.receiptOrderId.textContent === orderId) {
      el.receiptOrderStatus.textContent = 'BATAL';
      el.receiptOrderStatus.className = 'status-badge';
    }
    await loadMenus({ quiet: true });
  } catch (error) {
    showNotice(error.message || 'Pembatalan gagal. Periksa ID pesanan dan statusnya.', 'error');
  } finally {
    state.submittingCancel = false;
    el.cancelButton.disabled = false;
    el.cancelButton.innerHTML = '<span aria-hidden="true">↺</span> Batalkan pesanan';
  }
}

function handleQuantityClick(event) {
  const button = event.target.closest('button[data-action][data-id]');
  if (!button) return;
  const { id, action } = button.dataset;
  if (action === 'remove') {
    state.cart.delete(id);
    renderMenus();
    renderCart();
    return;
  }
  updateQuantity(id, action === 'increase' ? 1 : -1);
}

el.menuGrid.addEventListener('click', handleQuantityClick);
el.cartItems.addEventListener('click', handleQuantityClick);
el.checkoutForm.addEventListener('submit', processCheckout);
el.cancelForm.addEventListener('submit', cancelOrder);
el.refreshMenusButton.addEventListener('click', () => loadMenus());
el.menuSearch.addEventListener('input', (event) => {
  state.search = event.target.value;
  renderMenus();
});
el.printReceiptButton.addEventListener('click', () => window.print());
document.addEventListener('keydown', (event) => {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault();
    el.menuSearch.focus();
  }
});

renderCurrentDate();
loadMenus();
