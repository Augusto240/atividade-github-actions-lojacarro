// Wrapper de fetch que injeta o token JWT e trata erros comuns da API.

async function apiFetch(path, options = {}) {
    const headers = Object.assign(
        { "Content-Type": "application/json" },
        options.headers || {}
    );

    const token = getToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers
    });

    if (response.status === 401) {
        limparSessao();
        window.location.href = "index.html";
        throw new Error("Sessão expirada. Faça login novamente.");
    }

    if (response.status === 429) {
        throw new Error("Muitas requisições em pouco tempo. Aguarde alguns segundos e tente de novo.");
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
        ? await response.json().catch(() => ({}))
        : null;

    if (!response.ok) {
        throw new Error((data && data.error) || `Erro na requisição (status ${response.status}).`);
    }

    return data;
}

const CarroApi = {
    listar(marca) {
        const query = marca ? `?marca=${encodeURIComponent(marca)}` : "";
        return apiFetch(`/carro${query}`);
    },
    buscarPorId(id) {
        return apiFetch(`/carro/${id}`);
    },
    criar(carro) {
        return apiFetch("/carro/salvar", {
            method: "POST",
            body: JSON.stringify(carro)
        });
    },
    atualizar(id, carro) {
        return apiFetch(`/carro/${id}`, {
            method: "PUT",
            body: JSON.stringify(carro)
        });
    },
    remover(id) {
        return apiFetch(`/carro/${id}`, {
            method: "DELETE"
        });
    }
};
