package com.propertyview.domain.port;

import com.propertyview.domain.model.Address;

/**
 * outbound port for verifying a hotel's physical address against an external HTTP service
 */
public interface AddressVerificationPort {
    boolean verifyAddress(Address address);
}
