package de.henku.computations;


public interface SecureComputationFactory<T extends AbstractSecureComputationSlave> {

	public T create();
}
