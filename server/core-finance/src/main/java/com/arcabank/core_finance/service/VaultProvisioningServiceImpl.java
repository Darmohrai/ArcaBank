package com.arcabank.core_finance.service;

import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.grpc.ForgeVaultRequest;
import com.arcabank.grpc.ForgeVaultResponse;
import com.arcabank.grpc.VaultProvisioningServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Random;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class VaultProvisioningServiceImpl extends VaultProvisioningServiceGrpc.VaultProvisioningServiceImplBase {

    private final AccountRepository accountRepository;

    @Override
    public void forgeInitialVault(ForgeVaultRequest request, StreamObserver<ForgeVaultResponse> responseObserver) {
        log.info("Forging initial vault requested by auth-service. User ID: {}, Currency: {}",
            request.getUserId(), request.getCurrencyCode());

        try {
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.fromString(request.getCurrencyCode());

            String accountType = "CHECKING";

            String iban = generateIban();

            accountRepository.createAccount(accountId, userId, iban, accountType, request.getCurrencyCode());

            ForgeVaultResponse response = ForgeVaultResponse.newBuilder()
                .setSuccess(true)
                .setVaultId(accountId.toString())
                .setMessage("Account successfully created. IBAN: " + iban)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Account created successfully: {} with IBAN: {}", accountId, iban);
        } catch (Exception e) {
            log.error("Failed to create account", e);

            ForgeVaultResponse errorResponse = ForgeVaultResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Internal error during account creation: " + e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    private String generateIban() {
        Random random = new Random();
        int checkDigits = random.nextInt(90) + 10;
        String mfoCode = "305299";

        StringBuilder accountDigits = new StringBuilder();
        for (int i = 0; i < 19; i++) {
            accountDigits.append(random.nextInt(10));
        }

        return "UA" + checkDigits + mfoCode + accountDigits.toString();
    }
}
