const loginForm = document.querySelector("#login-form");
const loginButton = document.querySelector("#login-button");
const loginStatus = document.querySelector("#login-status");

const parameters = new URLSearchParams(window.location.search);

if (parameters.has("error")) {
  loginStatus.textContent = "E-posta veya şifre hatalı.";
} else if (parameters.has("logout")) {
  loginStatus.textContent = "Çıkış yaptın.";
} else if (parameters.has("registered")) {
  loginStatus.textContent = "Hesabın oluşturuldu. Şimdi giriş yapabilirsin.";
}

async function prepareLogin() {
  try {
    const response = await fetch("/api/csrf", {
      credentials: "same-origin",
      cache: "no-store",
    });

    if (!response.ok) {
      throw new Error("Giriş formu hazırlanamadı.");
    }

    const csrf = await response.json();

    const input = document.createElement("input");
    input.type = "hidden";
    input.name = csrf.parameterName;
    input.value = csrf.token;

    loginForm.append(input);
    loginButton.disabled = false;
  } catch {
    loginStatus.textContent =
      "Sunucuya ulaşılamadı. Sayfayı yenileyerek tekrar dene.";
  }
}

loginForm.addEventListener("submit", () => {
  loginButton.disabled = true;
  loginStatus.textContent = "Giriş yapılıyor…";
});

prepareLogin();
