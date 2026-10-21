const registerForm = document.querySelector("#register-form");
const registerStatus = document.querySelector("#register-status");
const submitButton = registerForm.querySelector('button[type="submit"]');

let submitting = false;

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (submitting) return;

  const displayName = document.querySelector("#display-name").value.trim();
  const email = document.querySelector("#email").value.trim();
  const password = document.querySelector("#password").value;
  const confirmation = document.querySelector("#password-confirm").value;

  if (!displayName) {
    registerStatus.textContent = "Lütfen adını yaz.";
    return;
  }

  if (password !== confirmation) {
    registerStatus.textContent = "Şifreler aynı olmalı.";
    return;
  }

  if (new TextEncoder().encode(password).length > 72) {
    registerStatus.textContent = "Şifre çok uzun. Daha kısa bir şifre kullan.";
    return;
  }

  submitting = true;
  submitButton.disabled = true;
  registerStatus.textContent = "Hesap oluşturuluyor…";

  try {
    const csrfResponse = await fetch("/api/csrf", {
      credentials: "same-origin",
      cache: "no-store",
    });

    if (!csrfResponse.ok) {
      throw new Error("Doğrulama bilgisi alınamadı. Sayfayı yenile.");
    }

    const csrf = await csrfResponse.json();

    const response = await fetch("/api/auth/register", {
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json",
        [csrf.headerName]: csrf.token,
      },
      body: JSON.stringify({ displayName, email, password }),
    });

    if (!response.ok) {
      const messages = {
        400: "Bilgilerini kontrol et. Şifre en az 12 karakter olmalı.",
        403: "İstek doğrulanamadı. Sayfayı yenileyip tekrar dene.",
        409: "Bu e-posta kullanımda olabilir. Başka bir adres dene.",
      };

      throw new Error(
        messages[response.status] ||
          "Hesap oluşturulamadı. Daha sonra tekrar dene.",
      );
    }

    registerForm.reset();
    window.location.assign("/login.html?registered");
  } catch (error) {
    registerStatus.textContent = error.message;
  } finally {
    submitting = false;
    submitButton.disabled = false;
  }
});
