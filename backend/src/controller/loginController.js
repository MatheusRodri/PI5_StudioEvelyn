import { Router } from "express";
import {criarCliente,login,atualizarCliente,exibirClientes,removerCliente } from "../repository/login.js";

const servidor = Router();

//Rota para criar um novo cliente
servidor.post('/cliente', async (req, res) => {
    try {
        const cliente = req.body; // Assumindo que o corpo da requisição contém o objeto cliente
        console.log(cliente);
        const novoCliente = await criarCliente(cliente);
        res.status(201).json(novoCliente);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Rota para realizar login
servidor.post('/login', async (req, res) => {
    try {
        const log = req.body; 
        const cliente = await login(log);
        console.log(cliente);
        res.status(200).json(cliente);
    } catch (error) {
        res.status(500).json( { error: error.message });
    }
});

// Rota para exibir todos os clientes
servidor.get('/cliente', async (req, res) => {
    try {
        const cliente = await exibirClientes();
        res.status(200).json(cliente);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Rota para remover um cliente
servidor.delete('/cliente', async (req, res) => {
    try {
        const cpf = req.body;
        const resposta = await removerCliente(cpf);
        res.status(200).json(resposta);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});


// Rota para atualizar um cliente
servidor.put('/cliente', async (req, res) => {
    try {
        const cliente = req.body;
        const resposta = await atualizarCliente(cliente);
        res.status(200).json(resposta);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});
export default servidor;