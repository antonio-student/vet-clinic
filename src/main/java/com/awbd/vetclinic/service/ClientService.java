package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ClientService extends BaseService<Client> {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        super(clientRepository, "Client");
        this.clientRepository = clientRepository;
    }

    public Client create(Client client) {
        log.info("Saving new client with name: {}", client.getName());
        return createEntity(client);
    }

    public Page<Client> getAllClients(Pageable pageable) {
        log.info("Fetching all clients with pagination");
        return getAllEntities(pageable);
    }

    public Page<Client> searchClients(String name, String email, Pageable pageable) {
        log.info("Searching clients with filters name={}, email={}", name, email);
        List<Specification<Client>> specifications = new ArrayList<>();
        addIfPresent(specifications, containsIgnoreCase("name", name));
        addIfPresent(specifications, containsIgnoreCase("email", email));
        Specification<Client> specification = specifications.stream()
                .reduce(Specification::and)
                .orElse(null);
        assert specification != null;
        return clientRepository.findAll(specification, pageable);
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

    private Specification<Client> containsIgnoreCase(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.trim().toLowerCase() + "%"
        );
    }

    private void addIfPresent(List<Specification<Client>> specifications, Specification<Client> specification) {
        if (specification != null) {
            specifications.add(specification);
        }
    }
}

