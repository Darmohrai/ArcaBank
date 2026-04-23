package com.arcabank.core_finance.service.grpc;

import com.arcabank.core_finance.convertor.ProtoCurrencyMapper;
import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.grpc.AccountProvisioningServiceGrpc;
import com.arcabank.grpc.CreateAccountRequest;
import com.arcabank.grpc.CreateAccountResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Random;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AccountProvisioningGrpcServer extends AccountProvisioningServiceGrpc.AccountProvisioningServiceImplBase {

    private final AccountRepository accountRepository;

    @Override
    public void createInitialAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        log.info("Provisioning initial account for User ID: {}", request.getUserId());

        String newIban = generateRandomIban();

        Account newAccount = Account.builder()
            .userId(UUID.fromString(request.getUserId()))
            .iban(newIban)
            .type(AccountType.CHECKING)
            .currency(ProtoCurrencyMapper.mapCurrency(request.getCurrency()))
            .build();

        accountRepository.createAccount(newAccount);

        CreateAccountResponse response = CreateAccountResponse.newBuilder()
            .setAccountId(newAccount.getId().toString())
            .setSuccess(true)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private String generateRandomIban() {
        Random random = new Random();
        StringBuilder iban = new StringBuilder("UA");
        iban.append(String.format("%02d", random.nextInt(99)));
        iban.append("305299");
        for (int i = 0; i < 19; i++) {
            iban.append(random.nextInt(10));
        }
        return iban.toString();
    }
}
