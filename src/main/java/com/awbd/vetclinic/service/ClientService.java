package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClientService extends BaseService<Client> {

    public ClientService(ClientRepository clientRepository) {
        super(clientRepository, "Client");
    }

    public Client create(Client client) {
        log.info("Saving new client with name: {}", client.getName());
        return createEntity(client);
    }

    public Page<Client> getAllClients(Pageable pageable) {
        log.info("Fetching all clients with pagination");
        return getAllEntities(pageable);
    }

    public Client getClientById(Long id) {
        log.info("Fetching client with id: {}", id);
        return getEntityById(id);
    }

    public Client update(Long id, Client clientDetails) {
        log.info("Updating client with id: {}", id);

        return updateEntity(id, clientDetails, (existingClient, details) -> {
            existingClient.setName(details.getName());
            existingClient.setPhone(details.getPhone());
            existingClient.setEmail(details.getEmail());
        });
    }

    public void deleteClient(Long id) {
        log.info("Deleting client with id: {}", id);
        deleteEntity(id);
    }
}

