// Importação das funcionalidades do express, cors e do controller
import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import agendamentoController from './controller/agendamentosController.js';
import loginController from './controller/loginController.js';

// Criação do servidor
const servidor = express();
servidor.use(cors());
servidor.use(express.json());
servidor.use(agendamentoController);
servidor.use(loginController);

// Middleware de tratamento de erros
servidor.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).send('Erro interno do servidor');
});


const port = process.env.PORT || 3000;
servidor.listen(port, () => console.log(`API rodando na porta ${port}`));

export default servidor;
