const householdStatus = document.querySelector("#household-status");
const membership = document.querySelector("#membership");
const householdOptions = document.querySelector("#household-options");
const createForm = document.querySelector("#create-household-form");
const joinForm = document.querySelector("#join-household-form");

let busy = false;

async function householdRequest(path = "", body) {
  const options = {
    credentials: "same-origin",
    cache: "no-store",
  };

  if (body !== undefined) {
    const csrfResponse = await fetch("/api/csrf", {
      credentials: "same-origin",
      cache: "no-store",
    });

    if (!csrfResponse.ok) {
      throw new Error("İstek doğrulanamadı. Sayfayı yenile.");
    }

    const csrf = await csrfResponse.json();

    options.method = "POST";
    options.headers = {
      "Content-Type": "application/json",
      [csrf.headerName]: csrf.token,
    };
    options.body = JSON.stringify(body);
  }

  const response = await fetch(`/api/household${path}`, options);

  if (
    response.status === 401 ||
    (response.redirected && new URL(response.url).pathname === "/login.html")
  ) {
    window.location.assign("/login.html");
    throw new Error("Tekrar giriş yapman gerekiyor.");
  }

  if (!response.ok) {
    const messages = {
      400: "Ev adını veya 32 karakterli davet kodunu kontrol et.",
      403: "İstek doğrulanamadı. Sayfayı yenile.",
      404: "Bu davet koduna ait ev bulunamadı.",
      409: "Zaten bir eve üyesin. Sayfayı yenile.",
    };

    throw new Error(
      messages[response.status] ||
        "İşlem tamamlanamadı. Daha sonra tekrar dene.",
    );
  }

  return response.json();
}

function showHousehold(household) {
  membership.hidden = !household.member;
  householdOptions.hidden = household.member;

  if (household.member) {
    document.querySelector("#household-name").textContent = household.name;
    document.querySelector("#invite-code").textContent = household.inviteCode;
  }
}

function setBusy(value) {
  busy = value;

  document.querySelectorAll("form input, form button").forEach((element) => {
    element.disabled = value;
  });
}

async function submitHousehold(path, body) {
  if (busy) return;

  setBusy(true);
  householdStatus.textContent = "İşlem yapılıyor…";

  try {
    const household = await householdRequest(path, body);
    showHousehold(household);
    householdStatus.textContent = "Ev üyeliğin hazır.";
  } catch (error) {
    householdStatus.textContent = error.message;
  } finally {
    setBusy(false);
  }
}

createForm.addEventListener("submit", (event) => {
  event.preventDefault();

  const name = document.querySelector("#household-name-input").value.trim();

  if (!name) {
    householdStatus.textContent = "Lütfen bir ev adı yaz.";
    return;
  }

  submitHousehold("", { name });
});

joinForm.addEventListener("submit", (event) => {
  event.preventDefault();

  const inviteCode = document.querySelector("#invite-code-input").value.trim();

  if (!/^[a-fA-F0-9]{32}$/.test(inviteCode)) {
    householdStatus.textContent = "Davet kodu 32 karakterli olmalı.";
    return;
  }

  submitHousehold("/join", { inviteCode });
});

async function loadHousehold() {
  try {
    const household = await householdRequest();
    showHousehold(household);
    householdStatus.textContent = "";
  } catch (error) {
    householdStatus.textContent = error.message;
  }
}

loadHousehold();
