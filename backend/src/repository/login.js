import con from "../connection.js";

// Função para criar um novo cliente
export async function criarCliente(cliente) {
    console.log(cliente);
    try {        
        const comando = `INSERT INTO 
                                    CLIENTES(CPF,NOME,EMAIL,SENHA)
		                            VALUES	(?,?,?,?)`
            ;
        const valores = [cliente.CPF, cliente.NOME, cliente.EMAIL, cliente.SENHA];
        const resp = await con.query(comando, valores);
        if (resp[0].affectedRows > 0) {
            return { message: "Cadastro realizado com sucesso !" };
        }
        return { message: "Erro ao cadastrar cliente!" };

    } catch (error) {
        throw new Error(error.message);
    }
}

export async function login(login) {
    try {
        const comando = `SELECT 
                            *
                        FROM CLIENTES
                        WHERE EMAIL = ? AND SENHA = ?`
            ;
        const valores = [login.EMAIL, login.SENHA];
        const resp = await con.query(comando, valores);
        return resp[0];

    } catch (error) {
        throw new Error(error.message);
    }
}

// Função para exibir todos os clientes
export async function exibirClientes() {
    try {
        const comando = `SELECT 
                            *
                        FROM CLIENTES`
            ;
        const resp = await con.query(comando);
        return resp[0];
    }catch (error) {
        throw new Error(error.message);
    }
}

// Função para remover um cliente
export async function removerCliente(cpf) {
    try {
        const comando = `DELETE FROM CLIENTES WHERE CPF = ?`;
        const valores = [cpf.CPF];
        
        await con.query(comando, valores);
    } catch (error) {
        throw new Error(error.message);
    }
}

// Função para atualizar um cliente
export async function atualizarCliente(cliente) {
    try {
        const comando = `UPDATE CLIENTES
                        SET NOME = ?, EMAIL = ?, SENHA = ?
                        WHERE CPF = ?`
            ;
        const valores = [cliente.NOME, cliente.EMAIL, cliente.SENHA, cliente.CPF];
        await con.query(comando, valores);
    }
    catch (error) {
        throw new Error(error.message);
    }
}