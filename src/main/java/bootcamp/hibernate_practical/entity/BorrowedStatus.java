package bootcamp.hibernate_practical.entity;

// An enum, not a boolean, so later states (e.g. LOST, IN_REPAIR) can be added
// without extra boolean fields that could contradict each other
public enum BorrowedStatus {
    NOT_BORROWED,
    BORROWED
}
