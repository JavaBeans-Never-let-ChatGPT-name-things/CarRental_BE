package com.example.backend.service;

public interface ContractService {
    String retryContractSuccess(Long contractId, String token);
    String retryContract(Long contractId, String token);
}
