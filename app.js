const STORAGE_KEY = "ortak-alisveris-products";

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

function loadProducts() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);

    if (saved === null) {
      return;
    }

    const parsed = JSON.parse(saved);

    if (
      !Array.isArray(parsed) ||
      !parsed.every(
        (product) =>
          product !== null &&
          typeof product === "object" &&
          typeof product.id === "string" &&
          typeof product.name === "string" &&
          typeof product.quantity === "string" &&
          typeof product.completed === "boolean",
      )
    ) {
      throw new Error("Geçersiz liste verisi");
    }

    products = parsed;
  } catch {
    status.textContent =
      "Kayıtlı liste okunamadı. Yeni eklediğin ürünler mevcut kaydın yerini alır.";
  }
}

function saveProducts(message) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(products));
    status.textContent = message;
  } catch {
    status.textContent =
      "Değişiklik ekranda uygulandı ancak kaydedilemedi. Sayfayı kapatırsan kaybolabilir.";
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
  toggleButton.setAttribute(
    "aria-label",
    `${product.name}: ${product.completed ? "alınacaklara geri taşı" : "alındı olarak işaretle"}`,
  );

  toggleButton.addEventListener("click", () => {
    product.completed = !product.completed;
    saveProducts("Ürünün durumu güncellendi.");
    renderProducts();
  });

  const deleteButton = document.createElement("button");
  deleteButton.type = "button";
  deleteButton.className = "delete-button";
  deleteButton.textContent = "Sil";
  deleteButton.setAttribute("aria-label", `${product.name} ürününü sil`);

  deleteButton.addEventListener("click", () => {
    products = products.filter((item) => item.id !== product.id);
    saveProducts("Ürün silindi.");
    renderProducts();
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

  products.push({
    id: crypto.randomUUID(),
    name,
    quantity,
    completed: false,
  });

  saveProducts(`${name} listeye eklendi.`);
  renderProducts();
  form.reset();
  nameInput.focus();
});

loadProducts();
renderProducts();
