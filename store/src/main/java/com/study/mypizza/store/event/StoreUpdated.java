package com.study.mypizza.store.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class StoreUpdated extends AbstractEvent {
}
