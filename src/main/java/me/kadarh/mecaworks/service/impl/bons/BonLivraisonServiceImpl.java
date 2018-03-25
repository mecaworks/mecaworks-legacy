package me.kadarh.mecaworks.service.impl.bons;

import lombok.extern.slf4j.Slf4j;
import me.kadarh.mecaworks.domain.bons.BonLivraison;
import me.kadarh.mecaworks.domain.others.Stock;
import me.kadarh.mecaworks.repo.bons.BonLivraisonRepo;
import me.kadarh.mecaworks.repo.others.ChantierRepo;
import me.kadarh.mecaworks.repo.others.EmployeRepo;
import me.kadarh.mecaworks.repo.others.StockRepo;
import me.kadarh.mecaworks.service.bons.BonLivraisonService;
import me.kadarh.mecaworks.service.exceptions.OperationFailedException;
import me.kadarh.mecaworks.service.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author kadarH
 */

@Service
@Transactional
@Slf4j
public class BonLivraisonServiceImpl implements BonLivraisonService {

	private StockRepo stockRepo;
	private ChantierRepo chantierRepo;
	private EmployeRepo employeRepo;
	private BonLivraisonRepo bonLivraisonRepo;

	public BonLivraisonServiceImpl(StockRepo stockRepo, ChantierRepo chantierRepo, EmployeRepo employeRepo, BonLivraisonRepo bonLivraisonRepo) {
		this.stockRepo = stockRepo;
		this.chantierRepo = chantierRepo;
		this.employeRepo = employeRepo;
		this.bonLivraisonRepo = bonLivraisonRepo;
	}

	@Override
	public BonLivraison add(BonLivraison bonLivraison) {
		try {
			// create the first stock Entree ( lgazoil li dkhol l chantier d'arrivée )
			Stock stock = new Stock();
			stock.setChantier(bonLivraison.getChantierArrivee());
			stock.setEntreeL(bonLivraison.getQuantite());
			stock.setDate(bonLivraison.getDate());

			// create the second stock Sortie ( lgazoil li khroj men chantier de depart )
			Stock stock1 = new Stock();
			stock1.setDate(bonLivraison.getDate());
			stock1.setChantier(bonLivraison.getChantierDepart());
			stock1.setSortieL(bonLivraison.getQuantite());
			stockRepo.saveAll(Arrays.asList(stock, stock1));

			//saving the bon and return it
			return bonLivraisonRepo.save(bonLivraison);
		} catch (Exception e) {
			throw new OperationFailedException("L'ajout du bon a echoué , opération echoué", e);
		}
	}

	@Override
	public BonLivraison getBon(Long id) {
		try {
			return bonLivraisonRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new ResourceNotFoundException("L'element n'existe pas dans la base, opération echouée", e);
		} catch (Exception e) {
			throw new OperationFailedException("Opération echouée", e);
		}
	}

	@Override
	public BonLivraison update(BonLivraison bonLivraison) {
		try {
			BonLivraison old = bonLivraisonRepo.findById(bonLivraison.getId()).get();
			if (bonLivraison.getChantierDepart() != null)
				old.setChantierDepart(bonLivraison.getChantierDepart());
			if (bonLivraison.getChantierArrivee() != null)
				old.setChantierArrivee(bonLivraison.getChantierArrivee());
			if (bonLivraison.getCode() != null)
				old.setCode(bonLivraison.getCode());
			if (bonLivraison.getDate() != null)
				old.setDate(bonLivraison.getDate());
			if (bonLivraison.getPompiste() != null)
				old.setPompiste(bonLivraison.getPompiste());
			if (bonLivraison.getQuantite() != null)
				old.setTransporteur(bonLivraison.getTransporteur());
			return bonLivraisonRepo.save(old);
		} catch (NoSuchElementException e) {
			throw new ResourceNotFoundException("Le bon n'existe pas , opération échouée");
		} catch (Exception e) {
			throw new OperationFailedException("Opération echouée", e);
		}
	}

	@Override
	public List<BonLivraison> bonList() {
		try {
			return bonLivraisonRepo.findAll();
		} catch (NoSuchElementException e) {
			throw new ResourceNotFoundException("La liste des bon est vide , opération échouée");
		} catch (Exception e) {
			throw new OperationFailedException("Opération echouée", e);
		}
	}

	@Override
	public Page<BonLivraison> bonList(Pageable pageable, String search) {
		try {
			if (search.isEmpty()) {
				log.debug("fetching bonLivraison page");
				return bonLivraisonRepo.findAll(pageable);
			} else {
				log.debug("Searching by :" + search);
				//creating example
				//Searching by code bon , nom chantier, nom fournisseur

				BonLivraison bonLivraison = new BonLivraison();

				if (chantierRepo.findByNom(search).isPresent()) {
					bonLivraison.setChantierArrivee(chantierRepo.findByNom(search).get());
					bonLivraison.setChantierDepart(chantierRepo.findByNom(search).get());
				}
				if (employeRepo.findByNom(search).isPresent()) {
					bonLivraison.setTransporteur(employeRepo.findByNom(search).get());
					bonLivraison.setPompiste(employeRepo.findByNom(search).get());
				}
				bonLivraison.setDate(LocalDate.now());
				bonLivraison.setCode(search);

				//creating matcher
				ExampleMatcher matcher = ExampleMatcher.matchingAny()
						.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
						.withIgnoreCase()
						.withIgnoreNullValues();
				Example<BonLivraison> example = Example.of(bonLivraison, matcher);
				Pageable pageable1 = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "date"));
				log.debug("getting search results");
				return bonLivraisonRepo.findAll(example, pageable1);
			}
		} catch (Exception e) {
			log.debug("Failed retrieving list of bons de Lisvraison");
			throw new OperationFailedException("Operation échouée", e);
		}
	}

	@Override
	public void delete(Long id) {
		try {
			bonLivraisonRepo.delete(bonLivraisonRepo.findById(id).get());
		} catch (NoSuchElementException e) {
			throw new ResourceNotFoundException("Le bon n'existe pas , opération échouée");
		} catch (Exception e) {
			throw new OperationFailedException("Opération echouée", e);
		}
	}
}