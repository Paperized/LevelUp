package it.ilogreco.levelup.data;

/**
 * Data object that represents a rewards after completion
 */
public class GainedExperienceResult {
    public boolean wasGiven;
    public int experienceGained;
    public int additionalExperience;
    public int experienceEachCategory;
    public float bonusPercentage;

    public int getTotalExperience() {
        int bonus = (int)(experienceGained * bonusPercentage);
        return experienceGained + bonus + additionalExperience;
    }

    @Override
    public String toString() {
        return "GainedExperienceResult{" +
                "wasGiven=" + wasGiven +
                ", experienceGained=" + experienceGained +
                ", additionalExperience=" + additionalExperience +
                ", experienceEachCategory=" + experienceEachCategory +
                ", bonusPercentage=" + bonusPercentage +
                '}';
    }
}
