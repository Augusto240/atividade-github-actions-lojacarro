exigirLogin();

const tabelaCarros = document.getElementById("tabela-carros");
const mensagem = document.getElementById("mensagem");
const thAcoes = document.getElementById("th-acoes");
const areaGerente = document.getElementById("area-gerente");
const formCarro = document.getElementById("form-carro");
const carroIdInput = document.getElementById("carro-id");
const marcaInput = document.getElementById("marca");
const modeloInput = document.getElementById("modelo");
const anoInput = document.getElementById("ano");
const tituloForm = document.getElementById("titulo-form");
const btnCancelarEdicao = document.getElementById("btn-cancelar-edicao");

document.getElementById("usuario-logado").textContent = getEmailLogado() + " ";
document.getElementById("badge-role").textContent = getRoleLogado();

document.getElementById("btn-logout").addEventListener("click", logout);

if (isGerente()) {
    areaGerente.style.display = "block";
    thAcoes.style.display = "table-cell";
}

function mostrarMensagem(texto, tipo) {
    mensagem.innerHTML = `<div class="${tipo}">${texto}</div>`;
    setTimeout(() => { mensagem.innerHTML = ""; }, 4000);
}

function escapeHtml(valor) {
    const div = document.createElement("div");
    div.textContent = valor;
    return div.innerHTML;
}

function renderizarCarros(carros) {
    tabelaCarros.innerHTML = "";

    if (!carros || carros.length === 0) {
        const linhaVazia = document.createElement("tr");
        linhaVazia.innerHTML = `<td colspan="5">Nenhum carro encontrado.</td>`;
        tabelaCarros.appendChild(linhaVazia);
        return;
    }

    carros.forEach((carro) => {
        const linha = document.createElement("tr");
        let acoesHtml = "";
        if (isGerente()) {
            acoesHtml = `
                <td>
                    <button class="btn-secondary btn-small" onclick="editarCarro(${carro.id})">Editar</button>
                    <button class="btn-danger btn-small" onclick="removerCarro(${carro.id})">Excluir</button>
                </td>`;
        }

        linha.innerHTML = `
            <td>${carro.id}</td>
            <td>${escapeHtml(carro.marca)}</td>
            <td>${escapeHtml(carro.modelo)}</td>
            <td>${carro.ano}</td>
            ${acoesHtml}
        `;
        tabelaCarros.appendChild(linha);
    });
}

async function carregarCarros(marca) {
    try {
        const carros = await CarroApi.listar(marca);
        renderizarCarros(carros);
    } catch (erro) {
        mostrarMensagem(erro.message, "error");
    }
}

document.getElementById("btn-buscar").addEventListener("click", () => {
    const marca = document.getElementById("filtro-marca").value.trim();
    carregarCarros(marca || undefined);
});

document.getElementById("btn-limpar").addEventListener("click", () => {
    document.getElementById("filtro-marca").value = "";
    carregarCarros();
});

function limparFormulario() {
    carroIdInput.value = "";
    marcaInput.value = "";
    modeloInput.value = "";
    anoInput.value = "";
    tituloForm.textContent = "Cadastrar carro";
    btnCancelarEdicao.style.display = "none";
}

async function editarCarro(id) {
    try {
        const carro = await CarroApi.buscarPorId(id);
        carroIdInput.value = carro.id;
        marcaInput.value = carro.marca;
        modeloInput.value = carro.modelo;
        anoInput.value = carro.ano;
        tituloForm.textContent = `Editando carro #${carro.id}`;
        btnCancelarEdicao.style.display = "inline-block";
        window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (erro) {
        mostrarMensagem(erro.message, "error");
    }
}

async function removerCarro(id) {
    if (!confirm("Tem certeza que deseja excluir este carro?")) {
        return;
    }
    try {
        await CarroApi.remover(id);
        mostrarMensagem("Carro removido com sucesso.", "success");
        carregarCarros();
    } catch (erro) {
        mostrarMensagem(erro.message, "error");
    }
}

btnCancelarEdicao.addEventListener("click", limparFormulario);

if (formCarro) {
    formCarro.addEventListener("submit", async (event) => {
        event.preventDefault();

        const carro = {
            marca: marcaInput.value.trim(),
            modelo: modeloInput.value.trim(),
            ano: parseInt(anoInput.value, 10)
        };

        const id = carroIdInput.value;

        try {
            if (id) {
                await CarroApi.atualizar(id, carro);
                mostrarMensagem("Carro atualizado com sucesso.", "success");
            } else {
                await CarroApi.criar(carro);
                mostrarMensagem("Carro cadastrado com sucesso.", "success");
            }
            limparFormulario();
            carregarCarros();
        } catch (erro) {
            mostrarMensagem(erro.message, "error");
        }
    });
}

carregarCarros();
