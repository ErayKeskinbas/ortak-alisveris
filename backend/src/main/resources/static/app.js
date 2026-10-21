const form = document.querySelector("#product-form");
const nameInput = document.querySelector("#product-name");
const quantityInput = document.querySelector("#product-quantity");
const pendingList = document.querySelector("#pending-list");
const completedList = document.querySelector("#completed-list");
const pendingCount = document.querySelector("#pending-count");
const completedCount = document.querySelector("#completed-count");
const pendingEmpty = document.querySelector("#pending-empty");
const completedEmpty = document.querySelector("#completed-empty");
const status = document.querySelector("#status");

let products = [];
let busy = false;
let loaded = false;

let listRevision = 0;
let refreshInProgress = false;

const syncStatus = document.querySelector("#sync-status");

async function request(path = "", options = {}) {
  const headers = new Headers(options.headers);
  const method = (options.method || "GET").toUpperCase();

  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    const csrfResponse = await fetch("/api/csrf", {
      credentials: "same-origin",
      cache: "no-store",
    });

    if (!csrfResponse.ok) {
      throw new Error("İstek doğrulama bilgisi alınamadı.");
    }

    const csrf = await csrfResponse.json();
    headers.set(csrf.headerName, csrf.token);
  }

  const response = await fetch(`/api/products${path}`, {
    ...options,
    headers,
    credentials: "same-origin",
    cache: "no-store",
  });

  if (
    response.status === 401 ||
    (response.redirected && new URL(response.url).pathname === "/login.html")
  ) {
    window.location.assign("/login.html");
    throw new Error("Tekrar giriş yapman gerekiyor.");
  }

  if (!response.ok) {
    throw new Error(`İşlem tamamlanamadı (HTTP ${response.status}).`);
  }

  if (response.status === 204) {
    return;
  }

  return response.json();
}

function setBusy(value) {
  busy = value;

  form.querySelectorAll("input, button").forEach((element) => {
    element.disabled = value || !loaded;
  });

  document.querySelectorAll(".product-actions button").forEach((button) => {
    button.disabled = value || !loaded;
  });
}

async function updateList(action, message) {
  if (busy) return;

  listRevision++;
  setBusy(true);

  try {
    await action();
    products = await request();
    renderProducts();
    status.textContent = message;
  } catch (error) {
    status.textContent = `${error.message} Listeyi yenileyerek son durumu kontrol et.`;
  } finally {
    setBusy(false);
  }
}

function createProductElement(product) {
  const item = document.createElement("li");
  item.className = product.completed ? "product completed" : "product";

  const info = document.createElement("div");
  info.className = "product-info";

  const name = document.createElement("strong");
  name.className = "product-name";
  name.textContent = product.name;
  info.append(name);

  if (product.quantity) {
    const quantity = document.createElement("span");
    quantity.className = "product-quantity";
    quantity.textContent = product.quantity;
    info.append(quantity);
  }

  const actions = document.createElement("div");
  actions.className = "product-actions";

  const toggleButton = document.createElement("button");
  toggleButton.type = "button";
  toggleButton.className = "toggle-button";
  toggleButton.textContent = product.completed ? "Geri al" : "Alındı";
  toggleButton.setAttribute("aria-label", `${product.name}: durumunu değiştir`);

  toggleButton.addEventListener("click", () => {
    updateList(
      () =>
        request(`/${product.id}`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ completed: !product.completed }),
        }),
      "Ürünün durumu güncellendi.",
    );
  });

  const deleteButton = document.createElement("button");
  deleteButton.type = "button";
  deleteButton.className = "delete-button";
  deleteButton.textContent = "Sil";
  deleteButton.setAttribute("aria-label", `${product.name} ürününü sil`);

  deleteButton.addEventListener("click", () => {
    updateList(
      () => request(`/${product.id}`, { method: "DELETE" }),
      "Ürün silindi.",
    );
  });

  actions.append(toggleButton, deleteButton);
  item.append(info, actions);

  return item;
}

function renderProducts() {
  pendingList.replaceChildren();
  completedList.replaceChildren();

  let completedTotal = 0;

  for (const product of products) {
    const item = createProductElement(product);

    if (product.completed) {
      completedList.append(item);
      completedTotal++;
    } else {
      pendingList.append(item);
    }
  }

  const pendingTotal = products.length - completedTotal;

  pendingCount.textContent = pendingTotal;
  completedCount.textContent = completedTotal;
  pendingEmpty.hidden = pendingTotal > 0;
  completedEmpty.hidden = completedTotal > 0;
}

form.addEventListener("submit", (event) => {
  event.preventDefault();

  const name = nameInput.value.trim();
  const quantity = quantityInput.value.trim();

  if (!name) {
    status.textContent = "Lütfen bir ürün adı yaz.";
    nameInput.focus();
    return;
  }

  updateList(async () => {
    await request("", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, quantity }),
    });

    form.reset();
  }, `${name} listeye eklendi.`);
});

async function start() {
  setBusy(true);
  status.textContent = "Liste yükleniyor…";

  try {
    const response = await fetch("/api/household", {
      credentials: "same-origin",
      cache: "no-store",
    });

    if (
      response.status === 401 ||
      (response.redirected && new URL(response.url).pathname === "/login.html")
    ) {
      window.location.assign("/login.html");
      return;
    }

    if (!response.ok) {
      throw new Error("Ev bilgisi alınamadı.");
    }

    const household = await response.json();

    if (!household.member) {
      window.location.assign("/household.html");
      return;
    }

    products = await request();
    loaded = true;
    renderProducts();
    status.textContent = "";
  } catch (error) {
    pendingEmpty.hidden = true;
    completedEmpty.hidden = true;
    status.textContent = `${error.message} Sunucuyu kontrol edip sayfayı yenile.`;
  } finally {
    setBusy(false);
  }
}

start();

const logoutButton = document.querySelector("#logout-button");

logoutButton.addEventListener("click", async () => {
  logoutButton.disabled = true;

  try {
    const response = await fetch("/api/csrf", {
      credentials: "same-origin",
      cache: "no-store",
    });

    if (!response.ok) {
      throw new Error("Çıkış yapılamadı. Tekrar dene.");
    }

    const csrf = await response.json();

    const logoutForm = document.createElement("form");
    logoutForm.method = "post";
    logoutForm.action = "/logout";
    logoutForm.hidden = true;

    const tokenInput = document.createElement("input");
    tokenInput.type = "hidden";
    tokenInput.name = csrf.parameterName;
    tokenInput.value = csrf.token;

    logoutForm.append(tokenInput);
    document.body.append(logoutForm);
    logoutForm.submit();
  } catch {
    status.textContent = "Çıkış yapılamadı. Sayfayı yenileyip tekrar dene.";
    logoutButton.disabled = false;
  }
});

async function refreshSharedList() {
  if (!loaded || busy || refreshInProgress || document.hidden) {
    return;
  }

  refreshInProgress = true;
  const revisionAtStart = listRevision;

  try {
    const latestProducts = await request();

    // Bu sırada kullanıcı işlem yaptıysa eski sonucu uygulama.
    if (busy || revisionAtStart !== listRevision) {
      return;
    }

    // Liste değişmediyse ekranı gereksiz yere yeniden oluşturma.
    if (JSON.stringify(products) !== JSON.stringify(latestProducts)) {
      products = latestProducts;
      renderProducts();
    }

    syncStatus.textContent =
      "Ortak liste güncel · 5 saniyede bir kontrol edilir.";
  } catch {
    syncStatus.textContent =
      "Liste güncellenemedi. Bağlantı geldiğinde tekrar denenecek.";
  } finally {
    refreshInProgress = false;
  }
}

setInterval(refreshSharedList, 5000);

document.addEventListener("visibilitychange", () => {
  if (!document.hidden) {
    refreshSharedList();
  }
});
