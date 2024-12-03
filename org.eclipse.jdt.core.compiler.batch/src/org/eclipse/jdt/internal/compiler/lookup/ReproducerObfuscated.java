package org.eclipse.jdt.internal.compiler.lookup;

class ReproducerObfuscated {

	interface Contribution extends Hair, Unit, Championship, Woman, Failure, DirtUnderTest, Platform {}
	interface Failure extends Childhood {}
	interface Childhood extends Preparation, Singer, Knowledge {}
	interface Preparation extends Queen, Teaching {}
	interface Teaching extends Professor, Signature, Friendship, Surgery {}
	interface Friendship extends Assumption, Signature, Professor {}
	interface Assumption extends Professor {}
	interface Professor extends Dirt {}
	interface Dirt extends Marriage {}
	interface Marriage extends Extent, Supermarket, Height, Chapter, Indication {}
	interface Extent extends Shopping, Entertainment, Height {}
	interface Shopping extends Bonus {}
	interface Bonus  {}
	interface Entertainment extends Indication {}
	interface Indication  {}
	interface Height  {}
	interface Chapter  {}
	interface Supermarket extends Information, Shopping {}
	interface Information extends Bonus {}
	interface Signature extends Dirt {}
	interface Surgery extends Week, Professor {}
	interface Week extends Professor {}
	interface Queen extends Temperature {}
	interface Temperature extends Teaching {}
	interface Singer extends Union, Advice, Departure {}
	interface Advice extends Office {}
	interface Office extends Professor {}
	interface Union extends Advice {}
	interface Departure extends Teaching {}
	interface Knowledge extends Revenue, Wealth {}
	interface Revenue extends Teaching {}
	interface Wealth extends Teaching {}
	interface Hair extends Teaching {}
	interface Woman extends Consequence, Appointment {}
	interface Consequence extends Knowledge, Teaching, Recipe, Office {}
	interface Recipe extends Singer {}
	interface Appointment extends Singer, Moment, Topic, Flight, Hat, Currency {}
	interface Flight extends Video {}
	interface Video extends Singer {}
	interface Currency extends Departure, Teaching {}
	interface Hat extends Recipe, Knowledge, Office, Teaching {}
	interface Topic extends Teaching {}
	interface Moment extends Departure, Teaching {}
	interface DirtUnderTest extends Appointment {}
	interface Platform extends Music, Ratio {}
	interface Ratio extends Elevator, Skill, Agreement, Homework {}
	interface Agreement extends Teaching {}
	interface Elevator extends Measurement, Childhood, Competition {}
	interface Competition extends Art, Engineering, Childhood {}
	interface Art extends Singer {}
	interface Engineering extends Singer {}
	interface Measurement extends Teaching, Cigarette {}
	interface Cigarette extends Teaching, Independence, Profession {}
	interface Profession extends Marriage {}
	interface Independence extends Marriage {}
	interface Homework extends HomeworkInstance, Dealer {}
	interface Dealer extends Marriage {}
	interface HomeworkInstance extends Physics, Uncle, System, HomeworkArtefact, Anxiety, Cookie, Competition, Session, Connection, Penalty, Relationship {}
	interface System extends Childhood {}
	interface Uncle extends Church {}
	interface Church extends Policy {}
	interface Policy extends Theory, Expression, Garbage, Salad, Blood, Woman {}
	interface Blood extends Instance, Teaching {}
	interface Instance extends Singer, Song {}
	interface Song extends Dirt {}
	interface Theory extends Teaching {}
	interface Salad extends Teaching {}
	interface Garbage extends Equipment, Preparation, Appointment, Sister, Historian, Union, Sample, Studio, Selection, Power, Pizza, Queen, Child, Recipe, Combination, Society, Enthusiasm {}
	interface Studio extends Singer {}
	interface Historian extends Singer {}
	interface Equipment extends Hat, Childhood, Combination, Instance, Camera, Bird, Union, Death, Recipe {}
	interface Bird extends Childhood {}
	interface Death extends Mixture, Philosophy {}
	interface Mixture extends Teaching {}
	interface Philosophy extends Teaching {}
	interface Combination extends Singer, Sample {}
	interface Sample extends Teaching {}
	interface Camera extends Singer, Queen {}
	interface Power extends Queen, Childhood {}
	interface Society extends Appointment {}
	interface Enthusiasm extends Appointment {}
	interface Selection extends Childhood, Queen {}
	interface Sister extends Childhood {}
	interface Pizza extends Teaching {}
	interface Child extends Appointment {}
	interface Expression extends Teaching {}
	interface Connection extends Policy {}
	interface HomeworkArtefact extends Policy {}
	interface Penalty extends Teaching {}
	interface Cookie extends Childhood {}
	interface Session extends Childhood {}
	interface Physics extends Insurance, Teaching {}
	interface Insurance extends Teaching {}
	interface Anxiety extends Policy {}
	interface Relationship extends Teaching, Insurance {}
	interface Skill extends Teaching {}
	interface Music extends Agreement, Skill, Elevator, Homework, Lab {}
	interface Lab extends Tongue {}
	interface Tongue extends Marriage {}
	interface Unit extends Teaching {}
	interface Championship extends Marriage {}
	interface Celebration extends Wife, Debt, Math {}
	interface Math  {}
	interface Wife extends Information, Language {}
	interface Language extends Bonus {}
	interface Debt  {}

	public static void main(String[] args) {
		Contribution ignore = method(new Celebration() {}, Contribution.class);
	}

	public static <T extends Marriage> T method(Celebration a, Class<T> b) {
		return null;
	}

}

