package arivihan.technologies.doubtbuzzter2.model;

import android.content.res.Resources;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.SubscriptSpan;

import java.util.ArrayList;
import java.util.Arrays;

public class MockAllQuestions {

    SpannableStringBuilder cs = new SpannableStringBuilder("X3 + X2");

    public static String onePhy1 = "A car is moving towards east with a speed of 25 km/h. To the driver of the car, a bus appears to move towards north with a speed of 25√3 km/h. What is the actual velocity of the bus?";
    public static String onePhy1Option1 = "50 km/h, 30° east of north";
    public static String onePhy1Option2 = "50 km/h, 30° north of east";
    public static String onePhy1Option3 = "25 km/h, 30° east of north";
    public static String onePhy1Option4 = "25 km/h, 30° north of east";
    public static int onePhy1A = 1;

    public static String onePhy2 = "At time t = 0, a particle starts moving along the x-axis. If its kinetic energy increases uniformly with time t, the net force acting on it must be";
    public static String onePhy2Option1 = "Constant";
    public static String onePhy2Option2 = "Proportional to t";
    public static String onePhy2Option3 = "proportional to √t";
    public static String onePhy2Option4 = "proportional to 1/√t";
    public static int onePhy2A = 4;


    public static String onePhy3 = "A trolley of mass 250 kg carrying a sand bag of 25 kg is moving uniformly with a speed of 35 km/h on a frictionless track. After some time, the sand starts falling out of a hole (in sand bag) on the trolley's floor at the rate of 0.05 kg/s. What is the speed of trolley after the entire sand bag is empty?";
    public static String onePhy3Option1 = "40 km/h";
    public static String onePhy3Option2 = "35 km/h";
    public static String onePhy3Option3 = "48 km/h";
    public static String onePhy3Option4 = "None of these";
    public static int onePhy3A = 2;


    public static String onePhy4 = "When a particle is moving in a vertical circle";
    public static String onePhy4Option1 = "its radial and tangential acceleration, both are constant";
    public static String onePhy4Option2 = "its radial and tangential acceleration, both are varying";
    public static String onePhy4Option3 = "its radial acceleration is constant but tangential acceleration is varying";
    public static String onePhy4Option4 = "its radial acceleration is varying but tangential acceleration is constant";
    public static int onePhy4A = 2;


    public static String onePhy5 = "A particle on a spring executes simple harmonic motion when it passes through the equilibrium position it has a speed v. The particle is stopped and then the oscillations are restarted so that it now passes through the equilibrium position with a speed of 2v. After this change the maximum displacement of the particle will change by a factor of";
    public static String onePhy5Option1 = "4";
    public static String onePhy5Option2 = "√8";
    public static String onePhy5Option3 = "2";
    public static String onePhy5Option4 = "√2";
    public static int onePhy5A = 3;


    public static String onePhy6 = "Calculate the force of attraction between two 90 kg spheres of metal spaced so that their centres are 40 cm apart";
    public static String onePhy6Option1 = "3.38 x 10^-6 N";
    public static String onePhy6Option2 = " 2.38 x 10^-4 N";
    public static String onePhy6Option3 = " 1.38 x10^-6 N";
    public static String onePhy6Option4 = " 4.38 x 10^-5 N";
    public static int onePhy6A = 1;


    //10-2
    public static String onePhy7 = "The potential energy of a particle varies with position x according to the relation U(x) = 2x⁴ - 27x, the point x = 3/2 is a point of";
    public static String onePhy7Option1 = "Unstable equilibrium";
    public static String onePhy7Option2 = "Stable equilibrium";
    public static String onePhy7Option3 = "Neutral equilibrium";
    public static String onePhy7Option4 = "Not in equilibrium";
    public static int onePhy7A = 2;


    public static String onePhy8 = "Two litres of water (density = 1 g/mL) in an open lid insulated kettle is heated by an electric heater of power 1 kW. The heat is lost from the lid at the rate of 160 J/s. The time taken for heating water (Specific heat capacity 4.2 kJK-1kg-1) from 20°C to 75°C is";
    public static String onePhy8Option1 = "340s";
    public static String onePhy8Option2 = "550s";
    public static String onePhy8Option3 = "620s";
    public static String onePhy8Option4 = "760s";
    public static int onePhy8A = 2;

    public static String onePhy9 = "A metal wire of length l and radius r is fixed between rigid supports. Initially, it is just taut. Now, due to decrease in temperature, the tension developed in the wire is";
    public static String onePhy9Option1 = "∝ l";
    public static String onePhy9Option2 = "∝ 1/l";
    public static String onePhy9Option3 = "∝ r²";
    public static String onePhy9Option4 = "∝ 1 / r²";
    public static int onePhy9A = 3;

    public static String onePhy10 = "Two identical tuning forks vibrate at 256 Hz. One of them is then loaded with wax after which 6 beats/s are heard. The period of the loaded tuning fork is";
    public static String onePhy10Option1 = "0.006s";
    public static String onePhy10Option2 = "0.005s";
    public static String onePhy10Option3 = "0.004s";
    public static String onePhy10Option4 = "0.003s";
    public static int onePhy10A = 3;

    public static String onePhy11 = "A particle is moving along a straight line with constant speed, then";
    public static String onePhy11Option1 = "velocity of particle is constant";
    public static String onePhy11Option2 = "acceleration of particle is zero";
    public static String onePhy11Option3 = "acceleration of particle is non-zero";
    public static String onePhy11Option4 = "None of the above";
    public static ArrayList<Integer> onePhy11A = new ArrayList<>(Arrays.asList(1,2));

    public static String onePhy12 = "Seven pulleys are connected with the help of 3 light strings as shown in figure. Consider P3, P4, P5 as light pulleys and pulleys P6 and P7 has masses m each. For this arrangement. Mark the correct statements";
    public static String onePhy12Option1 = "tension in the string connecting P1, P3 and P4 is zero";
    public static String onePhy12Option2 = "tension in the string connecting P1, P3 and P4 is mg/3";
    public static String onePhy12Option3 = "tension in all the 3 strings are same and equal to zero";
    public static String onePhy12Option4 = "acceleration of P6 is g downward and P7 is g upward";
    public static ArrayList<Integer> onePhy12A = new ArrayList<>(Arrays.asList(3,1));

    //

    public static String onePhy13 = "A pendulum bob of mass m connected to the end of " +
            "an ideal string of length l is released from rest from horizontal position as shown in figure. At the lowest point, the bob makes an elastic collision with a stationary block of mass 5m, which is kept on a frictionless surface. Mark the correct statement(s) for the instant just after the impact.";
    public static String onePhy13Option1 = "Tension in the string is 17/9mg";
    public static String onePhy13Option2 = "Tension in the string is 3mg";
    public static String onePhy13Option3 = "The velocity of block is √(2gl)/3";
    public static String onePhy13Option4 = "The maximum height attained by pendulum bob after the impact is(measured from lowest position) 4l/9";
    public static ArrayList<Integer> onePhy13A = new ArrayList<>(Arrays.asList(1,4,3));


    public static String onePhy14 = "The displacement of a particle varies according to the relation x = 3 sin 100t + 8 cos² 50t. Which of the following is/are correct about this motion";
    public static String onePhy14Option1 = "The motion of the particle is not SHM";
    public static String onePhy14Option2 = "The amplitude of the SHM of the particle is 5 units";
    public static String onePhy14Option3 = "The amplitude of resultant SHM is √73 units";
    public static String onePhy14Option4 = "The maximum displacement of the particle from the origin is 9 units";
    public static ArrayList<Integer> onePhy14A = new ArrayList<>(Arrays.asList(4,2));


    public static String onePhy15 = "Two coherent identical sound sources S1 and S2 (wavelength λ each) are kept at a distance 6λ from each other. Consider two circles C1 and C2 as shown in the figure.";
    public static String onePhy15Option1 = "Number of places, where maxima will be observed on C2 is 8";
    public static String onePhy15Option2 = "Number of such point on C1 will be 8";
    public static String onePhy15Option3 = "Number of places, where minima will be observed on C2 is 8";
    public static String onePhy15Option4 = "All of the above";
    public static ArrayList<Integer> onePhy15A = new ArrayList<>(Arrays.asList(1,3));

    public static String onePhy16 = "A block of mass M is placed on a smooth horizontal surface as shown in figure. The block is pulled by a light spring as shown in figure. If the ends A and B of the spring are moving with 4 m/s and 2 m/s respectively and the rate at which spring energy is increasing is 20 J/s, then determine the spring force.";
    public static int onePhy16A = 10;

    public static String onePhy17 = "Two blocks m1 = 4 kg and m2 = 2 kg connected by a weightless rod slide down a plane having an inclination of 37°. The coefficient of dynamic friction of m1 and m2 with the inclined plane are μ1= 0.75 and μ2 = 0.25 respectively. The tension in the rod is 2.6 pN Find the value of T. Take, sin 37 = 0.6 and cos 37 = 0.8.";
    public static int onePhy17A = 2;

    public static String onePhy18 = "A glass tube 1.5 m long and open at both ends is immersed vertically in a water tank completely. A tuning fork of 660 Hz is vibrated and kept at the upper end of tube and the tube is gradually raised out of water. The total number of resonances heard before when the tube comes out of water, (taking v = 330 ms -1) is ";
    public static int onePhy18A = 6;

    public static String onePhy19 = "A point object is moving with a speed of 5 m/s in front of a mirror moving with a speed of 2.5m/s as shown in the figure. The velocity of the image with respect to ground is √(10x), then x is";
    public static int onePhy19A = 9;

    public static String onePhy20 = "The electric field on two sides of a charged plate is shown in the figure. If the charge density on the plate is n*ε0, then n is  ";
    public static int onePhy20A = 4;



    public static String oneChe1 = "Xenon compound which cannot be prepared by direct interaction of the elements is";
    public static String oneChe1Option1 = "XeF2";
    public static String oneChe1Option2 = "XeO3";
    public static String oneChe1Option3 = "XeF4";
    public static String oneChe1Option4 = "XeF6";

    public static String oneChe2 = "The ion that cannot be precipitated by both HCl and H2S ?";
    public static String oneChe2Option1 = "Pb++";
    public static String oneChe2Option2 = "Cu+";
    public static String oneChe2Option3 = "Ag+";
    public static String oneChe2Option4 = "Sn++";


    public static String oneChe3 = "In a reaction a A -> products, the rate is doubled when the concentration of A is increased 4 times. If 50% of the reaction occurs in 1414 s, how long would it takes for the completion of 75% reaction?";
    public static String oneChe3Option1 = "4242s";
    public static String oneChe3Option2 = "2825s";
    public static String oneChe3Option3 = "2414s";
    public static String oneChe3Option4 = "2121s";

    public static String oneChe4 = "The E° in the given diagram is ";
    public static String oneChe4Option1 = "0.52V";
    public static String oneChe4Option2 = "0.61V";
    public static String oneChe4Option3 = "0.74V";
    public static String oneChe4Option4 = "0.83V";

    public static String oneChe5 = "The equilibrium constants K(p1) and K(p2), for the reactions X ⇌ 2Y and Z ⇌ P + Q, respectively are in the ratio of 1:9. If degree of dissociation of X and Z be equal then the ratio of total pressure at the equilibria is ";
    public static String oneChe5Option1 = "1:9";
    public static String oneChe5Option2 = "1:36";
    public static String oneChe5Option3 = "1:1";
    public static String oneChe5Option4 = "1:3";

    public static String oneChe6 = "The enthalpy of neutralisation of strong acid by strong base is -57.32 KJ mol-1. The enthalpy of formation of water is -285.84 kJ mol-1. The enthalpy of formation of hydroxyl ion is ";
    public static String oneChe6Option1 = "+228.52 KJ/mol";
    public static String oneChe6Option2 = "-114.26 KJ/mol";
    public static String oneChe6Option3 = "-228.52 KJ/mol";
    public static String oneChe6Option4 = "+114.26 KJ/mol";

    public static String oneChe7 = "Which of the following is wrong about perchlorate ion?";
    public static String oneChe7Option1 = "It's IUPAC name is tetraoxochlorate (VII)ion ";
    public static String oneChe7Option2 = "It's a very weak ligand";
    public static String oneChe7Option3 = "It's a stronger oxidising agent in acidic solution than that in basic solution";
    public static String oneChe7Option4 = "It's stronger oxidant than BrO4-";

    public static String oneChe8 = "The number of geometrical isomers of the complex [Cr(NH3)₂(H₂O)₂Br₂]+ is ";
    public static String oneChe8Option1 = "2";
    public static String oneChe8Option2 = "3";
    public static String oneChe8Option3 = "4";
    public static String oneChe8Option4 = "5";

    public static String oneChe9 = "For evaporation of water at 90°C";
    public static String oneChe9Option1 = "ΔH < T.ΔS";
    public static String oneChe9Option2 = "ΔH > T.ΔS";
    public static String oneChe9Option3 = "ΔH = T.ΔS";
    public static String oneChe9Option4 = "Cannot be corelated";

    public static String oneChe10 = "For pure water at 25°C and 50°C the correct statement is";
    public static String oneChe10Option1 = "pH at 25°C = pH at 50°C";
    public static String oneChe10Option2 = "pH at 25°C > pH at 50°C";
    public static String oneChe10Option3 = "pOH at 25°C = pOH at 50°C";
    public static String oneChe10Option4 = "pOH at 25°C < pOH at 50°C";

    public static String oneChe11 = "PseudoHalogenic acid(s) is/are";
    public static String oneChe11Option1 = "HN3";
    public static String oneChe11Option2 = "HCNO";
    public static String oneChe11Option3 = "HNCO";
    public static String oneChe11Option4 = "N2H4";

    public static String oneChe12 = "The compound which gives corresponding metal on strong heating is/are";
    public static String oneChe12Option1 = "AsH3";
    public static String oneChe12Option2 = "Ni(CO)4";
    public static String oneChe12Option3 = "MgCl2.6H2O";
    public static String oneChe12Option4 = "HgO";

    //

    public static String oneChe13 = "M CO3(s) ⇌ MO(s)+CO2(g) the equilibrium pressure of CO2 at 127°C and 147°C are 0.14 and 0.35 bar respectively ";
    public static String oneChe13Option1 = "The enthalpy of the reaction is nearly +64 Kj/mol";
    public static String oneChe13Option2 = "Increase in temperature decreases the yield of CO2 gas at equilibrium";
    public static String oneChe13Option3 = "It's an endothermic reaction";
    public static String oneChe13Option4 = "On addition of MCO3 at equilibrium, the equilibrium shifts towards right";

    public static String oneChe14 = "Consider the reduction reaction shown in Image";
    public static String oneChe14Option1 = "Racemic mixture of products is obtained";
    public static String oneChe14Option2 = "A carbanion reactive intermediate is involved";
    public static String oneChe14Option3 = "If hydrolysis is done using D₂O in place of H₂O, CH3CH2CH(OH)CH3 is obtained";
    public static String oneChe14Option4 = "If LiAlD4 is used in place of LiAlH4 CH3CH2CH(OD)CH3 is obtained";

    public static String oneChe15 = "Which of the following molecules are polar as well as planar";
    public static String oneChe15Option1 = "BFClBr";
    public static String oneChe15Option2 = "B3N3H6";
    public static String oneChe15Option3 = "ClF3";
    public static String oneChe15Option4 = "CH2=C=C=CF2";

    public static String oneChe16 = "A flask has 'N' gas molecules out of which N/10 molecules are moving at Z m/s, N/30 at Y m/s and the remaining ones are moving at X m/s. If rms velocity of the gas is 4.89 m/s under same conditions. What is X ?";

    public static String oneChe17 = "Mass of one atom of an element X is 2.32481x 10^–23 g. Express this mass in terms of amu.";

    public static String oneChe18 = "In the above reaction shown in figure, in the anionic part of the intermediate X the covalency of the central atom is";

    public static String oneChe19 = " The mono nitration will mainly take place at which position?";

    public static String oneChe20 = "How many types of aldehydes or ketones can be produced by the following reaction?";



    // First Mock Maths

    public static String oneMat1 = "The Inequality  x²-(4a + 3)x + (3a²+9a) < 0 (3 ≥ X ≥ 1) holds for ";
//    public static String oneMat1 = Resources.getSystem().getString(R.string.one_phy_3);
    public static String oneMat1Option1 = "no real value of a";
    public static String oneMat1Option2 = "all a satisfying 0 < a < 1/3";
    public static String oneMat1Option3 = "all a satisfying 1/3 < a < 2/3";
    public static String oneMat1Option4 = "all a satisfying belonging to (3/2, ∞)";

    public static String oneMat2 = "Let a and b be two real numbers. Suppose that the roots of the equation f(x) = x²-ax-b=0 have absolute value less than one (real or imaginary). Then, which of the following options is always correct?";
    public static String oneMat2Option1 = "|b| < 1, a+b < 1 and b-a < 1";
    public static String oneMat2Option2 = "|b| < 1, a+b < 1 and b-a > 1";
    public static String oneMat2Option3 = "|b| > 1, a+b < 1 and b-a < 1";
    public static String oneMat2Option4 = "|b| < 1, a+b > 1 and b-a < 1";


    public static String oneMat3 = "In ΔABC, B = π/3, C = π/4. Let a point D divides the side BC internally in the ratio 1:3, then the value of sin∠BAD/sin∠CAD is equal to";
    public static String oneMat3Option1 = "1/√6";
    public static String oneMat3Option2 = "1/3";
    public static String oneMat3Option3 = "1/√3";
    public static String oneMat3Option4 = "√(2/3)";

    public static String oneMat4 = "";
    public static String oneMat4Option1 = "π";
    public static String oneMat4Option2 = "π/2";
    public static String oneMat4Option3 = "π/4";
    public static String oneMat4Option4 = "2π";

    public static String oneMat5 = "The perimeter of a parallelogram is 40. All the sides of the parallelogram are natural numbers and at least one of its diagonals is a natural number. The number of non-congruent parallelograms which can be formed under the given conditions is equal";
    public static String oneMat5Option1 = "10";
    public static String oneMat5Option2 = "30";
    public static String oneMat5Option3 = "60";
    public static String oneMat5Option4 = "100";

    //fpi
    public static String oneMat6 = "The Value of integral in above image is equal to.. (where [.] denotes greatest integer function)";
    public static String oneMat6Option1 = "0";
    public static String oneMat6Option2 = "5π/4";
    public static String oneMat6Option3 = "7π/4";
    public static String oneMat6Option4 = "3π/4";

    public static String oneMat7 = "The value of n in the above equation of curve, such that the subnormal may be of constant length is";
    public static String oneMat7Option1 = "2";
    public static String oneMat7Option2 = "3/2";
    public static String oneMat7Option3 = "1/2";
    public static String oneMat7Option4 = "1";

    public static String oneMat8 = "If the normal to the curve y = f(x) at x = 0 be given by the equation 3x - y + 3 = 0, then the value of lim x->0  x² / { (f(x²) - 5f(4x²) + 4f(7x²)} is equal to";
    public static String oneMat8Option1 = "1/3";
    public static String oneMat8Option2 = "-1/3";
    public static String oneMat8Option3 = "-1/5";
    public static String oneMat8Option4 = "1/4";

    public static String oneMat9 = "When the terms in the binomial expansion (in above image) are Are arranged according to decreasing powers of x, the coefficients of the first three terms are in AP. The number of terms in the expansion with integral powers of x is equal to";
    public static String oneMat9Option1 = "1";
    public static String oneMat9Option2 = "2";
    public static String oneMat9Option3 = "3";
    public static String oneMat9Option4 = "4";

    public static String oneMat10 = "If unity is double repeated root of px³+q(x³+x)+r=0, then";
    public static String oneMat10Option1 = "p.r < 0";
    public static String oneMat10Option2 = "p.q < 0";
    public static String oneMat10Option3 = "p.q.r > 0";
    public static String oneMat10Option4 = "None of these";

    public static String oneMat11 = "f:R->R; f(x) is continuous function satisfying f(x) + f(x²) = 2, for all x belongs to R, then f(x) is";
    public static String oneMat11Option1 = "into";
    public static String oneMat11Option2 = "many-one";
    public static String oneMat11Option3 = "constant";
    public static String oneMat11Option4 = "periodic";

    public static String oneMat12 = "If the point (a,a²) lies inside the triangle formed by the lines x - y + 2 = 0, x + y = 2 and the x-axis, then the value of a can be equal to";
    public static String oneMat12Option1 = "-1";
    public static String oneMat12Option2 = "0";
    public static String oneMat12Option3 = "1";
    public static String oneMat12Option4 = "0.5";

    //

    public static String oneMat13 = "If above expression is expanded using binomial theorem, then which of the following options are correct ?";
    public static String oneMat13Option1 = "The number of irrational terms = 19 ";
    public static String oneMat13Option2 = "The middle term is irrational";
    public static String oneMat13Option3 = "The number of rational terms=2 ";
    public static String oneMat13Option4 = "10th term is rational";

    public static String oneMat14 = "For the curve y = b.(e^(x/a)), which of the following options are correct?";
    public static String oneMat14Option1 = "The subtangent is of constant length";
    public static String oneMat14Option2 = "The subnormal is of constant length";
    public static String oneMat14Option3 = "The subnormal varies as the square of ordinate";
    public static String oneMat14Option4 = "The subtangent varies as square of ordinate";

    public static String oneMat15 = " If two tangents can be drawn to the different branches of hyperbola x²/1 - y²/4 = 1  from the point (a,a²), then a can become equal to each and every point of which of the following intervals";
    public static String oneMat15Option1 = "(-2,0)";
    public static String oneMat15Option2 = "(0,2)";
    public static String oneMat15Option3 = "(-∞,-2)";
    public static String oneMat15Option4 = "(2,∞)";

    public static String oneMat16 = "Let x > 0, then the value of above limit, is equal to";

    public static String oneMat17 = "If f is continuous function satisfying f{f(x)} = 1+x, for all x belongs to R. Then, f'(1) is equal to";

    public static String oneMat18 = "If a,b,c are any three non-coplanar vectors and P1,P2...P6 are six permutations of a,b and c in the scalar triple product of [a,b,c], then the value of P1+P2 +..+P6 is equal";

    public static String oneMat19 = "The point P(1,2,3) is reflected in the xy-plane. If the image of the point P is Q, then OQ (where O is origin) is rotated by 180° in the plane containing OQ and x-axis about O to produce R. Finally, the point R is translated by 5 units in the positive direction of the y-axis to produce the point S. The sum of the coordinates of S is";

    public static String oneMat20 = "The length of the orthogonal projection of the line segment joining P(1,2,-1) and Q(2,1,-1) on the plane 2x - y + 2z + 3 = 0 is equal to";


    // Second Mock - Physics


    public static String twoPhy1 = "For a particle moving in a straight line velocity-displacement graph is shown in figure. Velocity-displacement graph is a circle of radius 2 units and centre is at (2, 0). The value of acceleration for this particle at point (2-√2,√2) will be (in SI units)?";
    public static String twoPhy1Option1 = "2";
    public static String twoPhy1Option2 = "√2";
    public static String twoPhy1Option3 = "4";
    public static String twoPhy1Option4 = "3";

    public static String twoPhy2 = "A Cylindrical vessel of 92 cm height is kept filled up to the brim. It has four holes 1,2,3,4 which are respectively at heights of 20 cm, 30 cm, 46 cm and 80 cm from the horizontal floor. The water falling at the maximum horizontal distance from the vessel comes from hole number ";
    public static String twoPhy2Option1 = "4";
    public static String twoPhy2Option2 = "3";
    public static String twoPhy2Option3 = "1";
    public static String twoPhy2Option4 = "2 & 4 both";


    public static String twoPhy3 = "The velocity of water near the surface of a river is 5 m/s whereas the depth is 5 m. The coefficient of viscosity of water is 10^-2 poise, then shearing stress will be in N/m²";
    public static String twoPhy3Option1 = "1";
    public static String twoPhy3Option2 = ".005";
    public static String twoPhy3Option3 = ".01";
    public static String twoPhy3Option4 = ".0025";

    public static String twoPhy4 = "A satellite of mass 5 M orbits the earth in a circular orbit. At one point in its orbit, the satellite explodes into two pieces, one of mass M and the other of mass 4M. After the explosion the mass M ends up travelling in the same circular orbit, but in opposite direction. After the explosion, motion of the mass 4M";
    public static String twoPhy4Option1 = "will be in a bound orbit";
    public static String twoPhy4Option2 = "will be in an unbound orbit";
    public static String twoPhy4Option3 = "will be in a partially bound orbit";
    public static String twoPhy4Option4 = "will not be possible to decide due to insufficient information";

    public static String twoPhy5 = "A semi-circular current carrying wire having radius R is placed in x-y plane with its centre at origin O. There is a position x dependent non-uniform magnetic field B= k.β.x / 2R (here, β is positive constant) existing in the region. The force due to magnetic field acting on the semi-circular wire will be along";
    public static String twoPhy5Option1 = "negative x-axis";
    public static String twoPhy5Option2 = "positive x-axis";
    public static String twoPhy5Option3 = "negative y-axis";
    public static String twoPhy5Option4 = "positive y-axis";

    public static String twoPhy6 = "Water is poured from a height of 10 m into an empty barrel at the rate of 1 L/s. If the weight of the empty barrel is 10 kg, then the weight indicated by weighing balance at time, t = 60s will be (barrel is kept on weighing balance)";
    public static String twoPhy6Option1 = "71.4 kg";
    public static String twoPhy6Option2 = "68.6 kg";
    public static String twoPhy6Option3 = "70.0 kg";
    public static String twoPhy6Option4 = "84.0 kg";

    public static String twoPhy7 = "What amount of heat will be generated in a coil of resistance R due to a charge q passing through it, if the current in the coil decreases down to zero uniformly during a time interval t? ";
    public static String twoPhy7Option1 = "4q²R/3t";
    public static String twoPhy7Option2 = "2q²R/3t";
    public static String twoPhy7Option3 = "3q²R/4t";
    public static String twoPhy7Option4 = "3q²R/2t";

    public static String twoPhy8 = "Determine the magnitude and direction of the net force acting on a stone of mass 0.2 kg, just after it is dropped from the window of a train which is accelerating at 5 m/s²? Take g = 10 m/s²";
    public static String twoPhy8Option1 = "2N downward";
    public static String twoPhy8Option2 = "√5N making an angle tan inverse 2 with train motion ";
    public static String twoPhy8Option3 = "2N upward";
    public static String twoPhy8Option4 = "√5N making an angle tan inverse 2 with Vertical ";

    public static String twoPhy9 = "The correct statement with respect to free expansion (expansion against Vacuum) is";
    public static String twoPhy9Option1 = "the intermediate stages of the free expansion are non-equilibrium stages";
    public static String twoPhy9Option2 = "we can apply 1st law of thermodynamics at initial and final equilibrium stages";
    public static String twoPhy9Option3 = "work done by the system on the surrounding is zero";
    public static String twoPhy9Option4 = "All of the above";

    public static String twoPhy10 = "Figure shows a boy on a horizontal platform A on a smooth horizontal surface, holding a rope attached to a box B. Boy pulls the rope with a constant force of 50 N. The combined mass of platform A and boy is 250 kg and that of box B, is 500 kg. The velocity of A relative to the box B, after 5s, the boy on A begins to pull the rope, will be";
    public static String twoPhy10Option1 = "1 m/s";
    public static String twoPhy10Option2 = "1.5 m/s";
    public static String twoPhy10Option3 = "2 m/s";
    public static String twoPhy10Option4 = "0.5 m/s";

    public static String twoPhy11 = "Pick up the correct statement (s)";
    public static String twoPhy11Option1 = " Area under a-t graph gives velocity";
    public static String twoPhy11Option2 = "Area under a-t graph gives change in velocity ";
    public static String twoPhy11Option3 = "Path of projectile as seen by another projectile is parabola ";
    public static String twoPhy11Option4 = "A body, whatever be its motion, is always at rest in a frame of reference fixed to body itself";

    public static String twoPhy12 = "A diminished image of an object is to be obtained on a screen 1.0 m away from it. This can be achieved by approximately placing ";
    public static String twoPhy12Option1 = "a convex mirror of suitable focal length";
    public static String twoPhy12Option2 = "a concave mirror of suitable focal length";
    public static String twoPhy12Option3 = "a convex lens of focal length less than 0.25 m";
    public static String twoPhy12Option4 = "a concave lens of suitable focal length";

    //

    public static String twoPhy13 = "A ball moving with velocity v hits a massive wall moving towards the ball with a velocity u. An elastic impact lasts for a time t ";
    public static String twoPhy13Option1 = "the average elastic force acting on the ball is m(u+v) / t";
    public static String twoPhy13Option2 = "the average elastic force acting on the ball is 2m(u+v) / t";
    public static String twoPhy13Option3 = "the kinetic energy of the ball increases by 2mu (u+v)";
    public static String twoPhy13Option4 = "The kinetic energy of the ball remains the same after collision";

    public static String twoPhy14 = "An ideal gas is in thermodynamic equilibrium, then throughout the gas";
    public static String twoPhy14Option1 = "same temperature everywhere";
    public static String twoPhy14Option2 = "same pressure everywhere";
    public static String twoPhy14Option3 = "same chemical composition";
    public static String twoPhy14Option4 = "None of these";

    public static String twoPhy15 = "Figure shows the variation of frequency of a characteristic X-rays and atomic number";
    public static String twoPhy15Option1 = "The characteristic X-Rays is k(beta)";
    public static String twoPhy15Option2 = "The characteristic X-Rays is k(alpha)";
    public static String twoPhy15Option3 = "The energy of photon emitted when this X-rays is emitted by a metal having z=101 is 204 keV";
    public static String twoPhy15Option4 = "The energy of photon emitted when this X-rays is emitted by a metal having z=101 is 102 keV";

    public static String twoPhy16 = "A particle is thrown with a speed 37.5 m/s horizontally from an inclined plane towards another inclined plane as shown in figure. It strikes perpendicularly on second inclined plane. If height h can be written as h = 25 * X m, then what is the value of X? Take g= 10 m/s² and neglect air resistance.";

    public static String twoPhy17 = "A sphere of mass M and radius R rests on two supports of the same height. One support is stationary while the other is moving with a velocity V = √2 m/s (see in the figure). Assume the stationary support is rough and the moving support is frictionless so that the sphere is slipping at B. At the instant shown, AB = R√2. Find the speed (in m/s) of the centre of the sphere at the given instant.";

    public static String twoPhy18 = "A ray is incident normally on a right angle prism whose μ is √3 and prism angle α = 30, after crossing prism ray passes through glass sphere. It strikes the glass sphere at R/√3 distance from principal axis, as shown in figure sphere is half polished. Find the angle of deviation of incident ray as multiplier of π";

    public static String twoPhy19 = "Consider the circuit shown in the figure. Capacitors A and B, each have capacitance C=2F. The plates of capacitor A are shorted using a wire of resistance R=1Ω while the left plate of capacitor B is given an initial charge Q= +4C. The switch is closed at a time t = 0. What will be the initial current (in ampere) drawn from the battery immediately after the switch is closed ?";

    public static String twoPhy20 = "Find the root mean square current of one cycle in an AC circuit in which instantaneous current is given by equation in the image";


    // Second Mock - Chemistry

    public static String twoChe1 = "An ideal solution of two liquids A and B is placed in a cylinder containing piston. Piston is pulled out isothermally so that the volume of the liquid decreases but that of vapours increases. Negligibly small amount of liquid was left and mole fraction of A in vapour is 0.4. If P°(A) 0.4 atm and P°(B) is 1.2 atm at the experimental temperature, which of the following is the total pressure at which the liquid is almost evaporated?";
    public static String twoChe1Option1 = "0.334 atm";
    public static String twoChe1Option2 = "0.667 atm";
    public static String twoChe1Option3 = "1 atm";
    public static String twoChe1Option4 = "2 atm";

    public static String twoChe2 = " A Certain reaction proceeds in a sequence of three elementary steps with the rate constants k1, k2 and k3. If the observed rate constant k(obs) of the reaction is expressed as shown in the image above. The observed energy of activation of the reaction is";
    public static String twoChe2Option1 = "E3 + (E1/2E2)";
    public static String twoChe2Option2 = "E2 + (E1-E3)/2";
    public static String twoChe2Option3 = "E3.√(E1/E2)";
    public static String twoChe2Option4 = "E3 + (E1-E2)/2";


    //fchhota2
    public static String twoChe3 = "H₂S is bubbled into 0.2 M NaCN solution which is 0.02 M in each [Cd(CN)4]2- and [Ag(CN)2]-. The H2S produces 10^(-9)M sulphide in the solution. Given in image";
    public static String twoChe3Option1 = "Ag₂S precipitates first from the solution";
    public static String twoChe3Option2 = "CdS precipitates first from the solution";
    public static String twoChe3Option3 = "None of them precipitate under the given conditions";
    public static String twoChe3Option4 = "Ag₂S precipitates at a sulphide concentration of 10^(-15)M";

    public static String twoChe4 = "Which of the following is V-shaped ?";
    public static String twoChe4Option1 = "S3--";
    public static String twoChe4Option2 = "I3-";
    public static String twoChe4Option3 = "N3-";
    public static String twoChe4Option4 = "None of these";

    public static String twoChe5 = "Which is correct about the three C-N bond lengths marked as a, b and c in the above molecules?";
    public static String twoChe5Option1 = "a=b>c";
    public static String twoChe5Option2 = "a>b>c";
    public static String twoChe5Option3 = "b>a>c";
    public static String twoChe5Option4 = "a=b=c";

    public static String twoChe6 = "Arrange the above in the order of reactivity towards an electrophillic attack";
    public static String twoChe6Option1 = "V > IV > III > II > I";
    public static String twoChe6Option2 = "III > V > IV > II > I";
    public static String twoChe6Option3 = "III > IV > V > II > I";
    public static String twoChe6Option4 = "V > IV > III > I > II";

    public static String twoChe7 = "Identify the corret statements regarding the structure of Al(BH4)3";
    public static String twoChe7Option1 = "Only 1,3,4";
    public static String twoChe7Option2 = "Only 1,2,3";
    public static String twoChe7Option3 = "Only 1,2,4";
    public static String twoChe7Option4 = "1,2,3 and 4";

    public static String twoChe8 = "Identify the incorrect statement for the above sequence of the reaction";
    public static String twoChe8Option1 = "D gives white dense fumes with conc. HCl";
    public static String twoChe8Option2 = "C is paramagnetic and F is diamagnetic";
    public static String twoChe8Option3 = "Both A and E are same compounds";
    public static String twoChe8Option4 = "Both D and F form a very important fertilizer";

    public static String twoChe9 = "If atoms of radius r form face-centred cubic unit cell of edge length a, the correct relation is";
    public static String twoChe9Option1 = "4r = √(3a)";
    public static String twoChe9Option2 = "4a = √(3r))";
    public static String twoChe9Option3 = "4r = √(2a))";
    public static String twoChe9Option4 = "2r = a";

    public static String twoChe10 = "One mole of the complex CoCl3.6H2O on reaction with excess of AgNO3 gives two moles of white precipitate. Thus, complex is";
    public static String twoChe10Option1 = "[Co(H2O)6]Cl3";
    public static String twoChe10Option2 = "[Co(H2O)5Cl]Cl2.H2O";
    public static String twoChe10Option3 = "[Co(H2O)4Cl2]Cl.2H2O";
    public static String twoChe10Option4 = "[Co(H2O)3Cl3]3H2O";

    public static String twoChe11 = "The species that undergoes disproportionation in an alkaline medium are";
    public static String twoChe11Option1 = "(MnO4)--";
    public static String twoChe11Option2 = "NO2";
    public static String twoChe11Option3 = "Cl2";
    public static String twoChe11Option4 = "ClO4-";

    public static String twoChe12 = "The reagent(s) for the foloowing conversion(?), is/are";
    public static String twoChe12Option1 = "alcoholic KOH";
    public static String twoChe12Option2 = "alcoholic KOH followed by NaNH2";
    public static String twoChe12Option3 = "acqueous KOH followed by NaNH2";
    public static String twoChe12Option4 = "Zn/CH3OH";

    //

    public static String twoChe13 = "Consider the following addition reaction on an enantiomerically pure bromo alkene. What is/are true regarding products of the above reaction";
    public static String twoChe13Option1 = "Four stereoisomers of products, each containing two chiral carbons are formed";
    public static String twoChe13Option2 = "A pair of enantionmers and a meso dibromide are formed";
    public static String twoChe13Option3 = "Only a pair of diastereomers of 2,3-dibromobutane are formed";
    public static String twoChe13Option4 = "One of the product is a meso-dibromide";

    public static String twoChe14 = "While phosphorus (P) has";
    public static String twoChe14Option1 = "P-P-P bond angle of 60 degree";
    public static String twoChe14Option2 = "4 lone pairs of electrons";
    public static String twoChe14Option3 = "4P-P single bonds";
    public static String twoChe14Option4 = "6P-P single bonds";

    public static String twoChe15 = "Which of the following statements are true ?";
    public static String twoChe15Option1 = "Equivalent weight of Ca(HC2O4)2 is M/2 when it is a reducing agent";
    public static String twoChe15Option2 = "Equivalent weight of Ca(HC2O4)2 is M when it behaves as an acid";
    public static String twoChe15Option3 = "Ca(HC2O4)2  can be estimated by MnO4-/H+";
    public static String twoChe15Option4 = "Ca(HC2O4)2  can be estimated by an acid";

    public static String twoChe16 = "Molar conductance of saturated BaSO4 solution is 400 omh-1cm²mol-1 and its specific conductance is 4 * 10^-5 ohm-1 cm-1. Hence, solubility product value of BaSO4 can be concluded as X * 10^-8 M². Identity X";

    public static String twoChe17 = "How many carbon-carbon double bonds are present in the end product, Z (see the above image)";

    public static String twoChe18 = "The reaction is Fe++ + Ag+ ⇌ Fe3+ + Ag. The equilibrium constant at 25°C for the reaction (Given, E°(Ag+/Ag) = 0.799 V and E°(Fe3+/Fe2+) = 0.771 V) is";

    public static String twoChe19 = "How many equivalents are there per mol of H₂S in its oxidation to SO₂ ?";

    public static String twoChe20 = "How many carbon atoms in the molecule HOOC-(CHOH)₂-COOH are asymmetric ?";


    // Second Mock - Maths


    public static String twoMat1 = "The sum of the above series upto 10 terms is equal to";
    public static String twoMat1Option1 = "1/2 - 1/(12*(3^10)) ";
    public static String twoMat1Option2 = "1/3 - 1/(12*(3^10)) ";
    public static String twoMat1Option3 = "1/2 - 1/(10*(3^10)) ";
    public static String twoMat1Option4 = "1/2 + 1/(12*(3^10)) ";

    public static String twoMat2 = "Sum of all the third order determinants that can be made with the real and distinct numbers 1, 2, 3, ..9 is equal to";
    public static String twoMat2Option1 = "9 times value of any one of the determinants";
    public static String twoMat2Option2 = "9! times value of any one of the determinants";
    public static String twoMat2Option3 = "9^9 times value of any one of the determinants";
    public static String twoMat2Option4 = "None of the above";


    public static String twoMat3 = "The number of irrational terms in expansion of above expression is equal to";
    public static String twoMat3Option1 = "47";
    public static String twoMat3Option2 = "56";
    public static String twoMat3Option3 = "50";
    public static String twoMat3Option4 = "6";

    public static String twoMat4 = "Consider a regular tetrahedron OABC with faces OAB, OBC, OCA and ABC having area of 4 sq units. Let v1, v2, v3, and v4 be the area vectors whose magnitudes are respectively, equal to areas of the faces OAB, OBC, OCA and ABC. If the directions of the vectors v1,v2,v3 and v4 are perpendicular to their respective faces pointing towards the outward direction, then the value of |v1 + v2 + v3 + v4| is equal to ";
    public static String twoMat4Option1 = "6";
    public static String twoMat4Option2 = "4";
    public static String twoMat4Option3 = "0";
    public static String twoMat4Option4 = "2";

    public static String twoMat5 = "If the lines (x-1)/1 = (y+3)/-k = (z-1)/k and 2x = (y-1) = (2-z) are coplanar, then the value of k is equal to";
    public static String twoMat5Option1 = "1";
    public static String twoMat5Option2 = "-1";
    public static String twoMat5Option3 = "2";
    public static String twoMat5Option4 = "-2";

    public static String twoMat6 = "The complex numbers z1,z2, and z3 form a right angled isosceles triangle. If the right angle is at the complex number z1 and z0 is the circumcentre of the triangle formed, then the value of z1z2 + z2z3 + z3z1 is equal to ";
    public static String twoMat6Option1 = "2z1² + z0²";
    public static String twoMat6Option2 = "2z0² + z1²";
    public static String twoMat6Option3 = "3(z0²+z1²)/2";
    public static String twoMat6Option4 = "(4z0²+2z1²)/3 ";

    public static String twoMat7 = "Let A be the vertex of the parabola y²=4ax. A chords AB is drawn to the parabola from A. Now, a line segment BC is drawn such that AB ⊥ BC and C lies on the axis of the parabola. The length of the projection of line segment BC on the axis of the parabola is equal to";
    public static String twoMat7Option1 = "a";
    public static String twoMat7Option2 = "2a";
    public static String twoMat7Option3 = "4a";
    public static String twoMat7Option4 = "8a";

    public static String twoMat8 = "The number of critical points of a function f, if f'(x) = (x³ + ax² + bx + c) |x|, where a,b,c belongs to R,c ≠ 0 and a² < 3b, is equal to ";
    public static String twoMat8Option1 = "2";
    public static String twoMat8Option2 = "4";
    public static String twoMat8Option3 = "1";
    public static String twoMat8Option4 = "3";

    public static String twoMat9 = "Number of integral values of k for which exactly three roots of the equation sin²x + (cosx -1)sinx - cosx - ksinx + k = 0, x ∈ (0,2π) are real, is equal to";
    public static String twoMat9Option1 = "0";
    public static String twoMat9Option2 = "3";
    public static String twoMat9Option3 = "4";
    public static String twoMat9Option4 = "2";

    public static String twoMat10 = "The internal bisector of the ∠A, ∠B and ∠C of a triangle ABC meet it's circumcircle at the points D,E and F respectively. The value of CA/DF is equal to";
    public static String twoMat10Option1 = "cos(B/2)";
    public static String twoMat10Option2 = "sin(B/2)";
    public static String twoMat10Option3 = "2sin(B/2)";
    public static String twoMat10Option4 = "2cos(B/2)";

    public static String twoMat11 = "If A and B are two matrices such that AB = B and BA = A, then which of the following options are correct?";
    public static String twoMat11Option1 = "(A+B)^10 = 2^9.(A+B)";
    public static String twoMat11Option2 = "(A+B)^10 = 2^10.(A+B)";
    public static String twoMat11Option3 = "(A+B)^11 = 2^11.(A+B)";
    public static String twoMat11Option4 = "(A+B)^11 = 2^10.(A+B)";

    public static String twoMat12 = "For the y²+2ax+2by+c=0, which of the following options are correct ?";
    public static String twoMat12Option1 = "It's an eclipse";
    public static String twoMat12Option2 = "It's a parabola";
    public static String twoMat12Option3 = "It's latusrectum = a";
    public static String twoMat12Option4 = "It's latusrectum = 2a";

    //

    public static String twoMat13 = "If for all possible real values of u and v, the relation 2f(u)cosv = f(u+v) + f(u-v) is always true, then which of the following options can be correct?";
    public static String twoMat13Option1 = "f(x)+f(-x) = 2a cosx, where a is constant";
    public static String twoMat13Option2 = "f(π-x) + f(-x) = 0";
    public static String twoMat13Option3 = "f(π-x) + f(x) = 2b sinx, where b is constant";
    public static String twoMat13Option4 = "f(x) = acosx + bsinx, where a and b are constant";

    public static String twoMat14 = "For above 2 limits, which of the following is correct ?";
    public static String twoMat14Option1 = "l+m=1";
    public static String twoMat14Option2 = "l+n=1";
    public static String twoMat14Option3 = "lmn=1";
    public static String twoMat14Option4 = " (m+n)/(m-n) = 1";

    public static String twoMat15 = "The possible integral value of 'a' such that the point of local minimum of the function f(x)=1+a²x-x³ satisfies the inequality (x²+5x+6)/(x²+x+2) < 0, is equal to";
    public static String twoMat15Option1 = "3";
    public static String twoMat15Option2 = "4";
    public static String twoMat15Option3 = "5";
    public static String twoMat15Option4 = "6";

    public static String twoMat16 = "";

    public static String twoMat17 = "If maximum and minimum values of the above determinant are a and b, then a+2b is equal to";

    public static String twoMat18 = "The equation to the side of triangle ABC is x=2. If the altitude through A meets the circumcircle of triange ABC at P(7,-2), then the absolute value of the sum of the x and y-coordinates of the orthocentre of triangle ABC is equal to";

    public static String twoMat19 = "If the value of f(x) is above limit, where 0 < x < π/2, then f(π/4) is equal to"; // pi

    public static String twoMat20 = "The total number of local minima and local maxima of above function is equal to";


  // Third Mock - Physics

    public static String threePhy1 = "A particle is moving along X-axis whose acceleration is given by a = 3x-4, where x is the location of the particle. At t=0, the particle is at rest at x=4/3. The distance travelled by the particle in 5s is";
    public static String threePhy1Option1 = "0";
    public static String threePhy1Option2 = "approx 42m";
    public static String threePhy1Option3 = "infinite";
    public static String threePhy1Option4 = "None of these";

    public static String threePhy2 = "Coefficient of mutual inductance for the given coil does not depend on";
    public static String threePhy2Option1 = "the relative orientation";
    public static String threePhy2Option2 = "their geometry";
    public static String threePhy2Option3 = "rate of which current changes in the coil";
    public static String threePhy2Option4 = "the medium in which the coil is";


    public static String threePhy3 = "A plank of mass 2 kg and length 1 m is placed on a horizontal floor. A small block of mass 1 kg is placed on top of the plank, at its right extreme end. The coefficient of friction between plank and floor is 0.5 and that between plank and block is 0.2. If a horizontal force = 30 N starts acting on the plank to the right, the time after which the block will fall off the plank in seconds is (g = 10 m/s²)";
    public static String threePhy3Option1 = "2/3";
    public static String threePhy3Option2 = "1.5";
    public static String threePhy3Option3 = "0.75";
    public static String threePhy3Option4 = "4/3";

    public static String threePhy4 = "The electric potential in a certain region of space is given by v = -3x² +4x, where x is in metres and v is in volts. In this region the equipotential surfaces are";
    public static String threePhy4Option1 = "planes parallel to x-y plane";
    public static String threePhy4Option2 = "planes parallel to y-z plane";
    public static String threePhy4Option3 = "concentric cylinder with x-axis as axis";
    public static String threePhy4Option4 = "concentric spheres centered at origin";

    //falpha
    public static String threePhy5 = "A resistance R carries a current I. The heat loss to the surroundings is a(T - T0), where 'a' is a constant, T is the temperature of the resistance and T0 is the temperature of atmosphere. If the coefficient of linear expansion is α, then strain in the resistance is";
    public static String threePhy5Option1 = "proportional to length of resistance wire";
    public static String threePhy5Option2 = "equal to αI²R/a";
    public static String threePhy5Option3 = "equal to αI²R/2a";
    public static String threePhy5Option4 = "equal to αIaR";

    public static String threePhy6 = "In an AC circuit, a resistance of 3 ohm, an inductance coil of 4 ohm and a condenser of 8 ohm  are connected in series with an AC source of 50 V(rms). The average power loss (in Watt) in the circuit will be";
    public static String threePhy6Option1 = "300";
    public static String threePhy6Option2 = "600";
    public static String threePhy6Option3 = "400";
    public static String threePhy6Option4 = "500";

    public static String threePhy7 = "An object is moving in a circle at a constant speed v. The magnitude of rate of  change of momentum of the object is";
    public static String threePhy7Option1 = "is zero";
    public static String threePhy7Option2 = "is proportional to v²";// fsquare
    public static String threePhy7Option3 = "is proportional to v";
    public static String threePhy7Option4 = "is proportional to v³";// fcube

    public static String threePhy8 = " Two particles of mass 1 kg and 3 kg move towards each other under their mutual force of attraction. No other force acts on them. When the relative velocity of approach of two particles is 2 m/s, their centre of mass has a velocity of 0.5 m/s. When relative velocity of approach becomes 3 m/s, the velocity(in m/s) of centre of mass will be ";
    public static String threePhy8Option1 = "0.75";
    public static String threePhy8Option2 = "0.5";
    public static String threePhy8Option3 = "10";
    public static String threePhy8Option4 = "1.25";

    public static String threePhy9 = "Two vibrating strings of the same material but length L and 2L have radii 2r and r respectively. They are stretched under the same tension. Both the strings vibrate in their fundamental modes, the one of length L with frequency v1, and other with frequency v2. The ratio v1/v2, is given by";
    public static String threePhy9Option1 = "1";
    public static String threePhy9Option2 = "1/2";
    public static String threePhy9Option3 = "3/2";
    public static String threePhy9Option4 = "2";

    public static String threePhy10 = "A source of sound of frequency 165 Hz generates sound waves which get reflected completely from a wall. A person standing at the wall starts moving away from the wall towards the source. The minimum distance of the point from the wall at which the person hear maximum sound is equal to (Velocity of sound = 330 m/s)";
    public static String threePhy10Option1 = "0.5m";
    public static String threePhy10Option2 = "0.75m";
    public static String threePhy10Option3 = "1m";
    public static String threePhy10Option4 = "1.5m";

    public static String threePhy11 = "A coin is dropped in an accelerating rail road car (as shown in figure). The path of the coin as observed from";
    public static String threePhy11Option1 = "ground frame of reference is parabola, if speed of the railroad car at the instant of dropping the coin is zero";
    public static String threePhy11Option2 = "ground frame of reference in parabola, if speed of the railroad car at the instant of dropping the coin is non-zero";
    public static String threePhy11Option3 = "ground frame of reference is straight line, if speed of the railroad car at the instant of dropping the coin is zero";
    public static String threePhy11Option4 = "railroad car frame of reference is a straight line";

    public static String threePhy12 = "For Gauss's law, mark the correct statement(s)";
    public static String threePhy12Option1 = "If we displace the enclosed charges (within a gaussian surface) without crossing the boundary, then E and Φ remains seme";
    public static String threePhy12Option2 = "If we displace the enclosed charges without crossing the boundary, then E will change but Φ Remains same";
    public static String threePhy12Option3 = "If charge crosses the boundary, then both E And Φ changes"; // phi
    public static String threePhy12Option4 = "If charge crosses the boundary, then Φ changes, but E remains same";

    //

    public static String threePhy13 = "In the circuit shown in figure, E1 and E2 are two ideal source of unknown emfs. Some currents are shown. Potential difference appearing across 6 ohm resistance is VA - VB = 10 V\n";
    public static String threePhy13Option1 = "the current in the 4.00 ohm resistor in between C and D is 5A";
    public static String threePhy13Option2 = "the unknown emf E1 is 36V";
    public static String threePhy13Option3 = "the unknown emf E2 is 54V";
    public static String threePhy13Option4 = "the resistance R is equal to 9 ohm";

    public static String threePhy14 = "When resonance is produced in a series L-C-R circuit,";
    public static String threePhy14Option1 = "impedance of circuit is maximum";
    public static String threePhy14Option2 = "current in the circuit is in phase with the applied voltage";
    public static String threePhy14Option3 = "inductive and capacitive reactance are equal";
    public static String threePhy14Option4 = "if R is reduced, the voltage across capacitor will increase";

    public static String threePhy15 = "Two identical blocks each of mass 2M are joined by means of a light spring of spring constant k. A man of mass M is standing on one of the block as shown in the diagram. If man jumps horizontally with a velocity v relative to block and horizontal surtace is smooth, then";
    public static String threePhy15Option1 = "the maximum compression in the spring is √(2M/k).(v/3)";
    public static String threePhy15Option2 = "man lands at horizontal distance v√(2H/g) from initial position of block";
    public static String threePhy15Option3 = "right block loses contact with wall when the elongation in spring is maximum";
    public static String threePhy15Option4 = "velocity of centre of mass of two block after 2M loses contact with wall is v/6";

    //fdegree
    public static String threePhy16 = "A container of volume 0.02 m³ contains a mixture of neon and argon gases at temperature of 27°C and pressure of 1x 10^5 Pascal. The total mass of the mixture is 28g. If the molecular weights of neon and argon are 20 and 40 respectively, determined mass of neon (in grams) in the mixture, assuming gases to be ideal (R = 8.314 J/mol- K)";

    public static String threePhy17 = "The linear charge density on the circumference of a ring of radius R is a= a0.sinθ, where θ is defined as shown in figure. Find the electric dipole moment (in C-m) of the ring if a0 = 1/π C/m and R = 1m";

    public static String threePhy18 = "Find the charge (in micro coloumb) on one of the capacitors, each having capacitance C = 1 microF, when a battery of emf E = 10 volts is connected across face diagonal of a cube as show in Figure. Resistance of each branch (except the branch which contains capacitor) of cube is R=10 ohm";

    public static String threePhy19 = "Two particles P1 and P2 of equal mass situated at (0,0) and (10,0) respectively at t=0 moving with constant velocities collided head on a point (4,0) after time t0. If the coefficient of restitution is 1. then what is the x-coordinate of centre of mass of the two particles at t=2t0?";

    public static String threePhy20 = "In the figure shown pulley and string are massless. The blocks move in vertical plane due to gravity, if the magnitude of acceleration of centre of mass of block is a (in m/s²), then value of 2a is (take g = 10 m/s²)";


    // Third Mock - Chemistry

    public static String threeChe1 = "At moderate pressure, the compressibility factor for a particular gas is given by z = 1 + 0.34p - 170p/T (p in bar and T in Kelvin).The Boyle's temperature of this gas is ";
    public static String threeChe1Option1 = "298K";
    public static String threeChe1Option2 = "170K";
    public static String threeChe1Option3 = "500K";
    public static String threeChe1Option4 = "340K";

    //fdelta
    //freversible
    public static String threeChe2 = "A reaction at 300K with ΔG°=-1743 J/mol consists of 3 moles of A(g), 6 moles of B(g) and 3 moles of C(g). If A,B and C are in equilibrium in 1 L container then the reaction may be [Given, e^0.7 = 2, R = 8.3 J/K-mol] ";
    public static String threeChe2Option1 = "A + B  ⇌ C";
    public static String threeChe2Option2 = "A  ⇌ B + 2C";
    public static String threeChe2Option3 = "2A  ⇌ B + C";
    public static String threeChe2Option4 = "A + B  ⇌ 2C";


    public static String threeChe3 = "Which of the following process is not involved in extraction of Fe ?";
    public static String threeChe3Option1 = "Gravity separation";
    public static String threeChe3Option2 = "Leaching";
    public static String threeChe3Option3 = "Roasting";
    public static String threeChe3Option4 = "Carbon reduction";

    public static String threeChe4 = "Give the IUPAC name of 'B' is";
    public static String threeChe4Option1 = "2-butanal";
    public static String threeChe4Option2 = "3-hydroxy butanal";
    public static String threeChe4Option3 = "3-formyl-2-propanol";
    public static String threeChe4Option4 = "4-oxo-2-propanol";

    public static String threeChe5 = "Which of the following type of compound is used to increase the hardness of the silicon polymer";
    public static String threeChe5Option1 = "R2SiCl2";
    public static String threeChe5Option2 = "RSiCl3";
    public static String threeChe5Option3 = "R3SiCl";
    public static String threeChe5Option4 = "None of these";

    public static String threeChe6 = "The apparent rate constant for a reaction is K(app) = k1C / (1+αC) where, C is the concentration of the reactant, k1 and α are contants. Calculate the value of C (in mol/L) for which K(app) has 90% of its limiting value at C tending to very large values. (Given, α = 9 * 10^5)";
    public static String threeChe6Option1 = "10^-6";
    public static String threeChe6Option2 = "10^-4";
    public static String threeChe6Option3 = "10^-5";
    public static String threeChe6Option4 = "5 * 10^-5";

    public static String threeChe7 = "Statement 1: In diamond, carbon atoms occupy alternate tetrahedral voids in the FCC lattice formed by the carbon atoms.\n" +
            "Statement 2: In diamond, packing fraction is more than 74%";
    public static String threeChe7Option1 = "Statement:1 is true, statement-2 is true and statement-2 is correct explanation for statement-1";
    public static String threeChe7Option2 = "Statement:1 is true, statement-2 is true and statement-2 is not the correct explanation for statement-1";
    public static String threeChe7Option3 = "Statement-1 is true, statement-2 is false ";
    public static String threeChe7Option4 = "Statement-2 is true, statement-1 is false ";

    public static String threeChe8 = "5 mole H2O(l) at 373 K and 1 atm is converted into H2O(g) at 373 K and 5 atm. ΔG for this process is (Given R = 2 cal/K-mol)";
    public static String threeChe8Option1 = "0";
    public static String threeChe8Option2 = "1865 in 5 cal";
    public static String threeChe8Option3 = "3750 in 5 cal";
    public static String threeChe8Option4 = "-3730 in 5 cal";

    public static String threeChe9 = "The first ionisation energy of Na, NO, Xe and O₂ follows the order Na< NO< Xe=O₂. O₂ reacts with powerful oxidising agent (PtF6) to yield O₂+[PtF6]. If PtF6 is allowed to react with other mentioned species then the product is/are";
    public static String threeChe9Option1 = "Only Na+[PtF6]-";
    public static String threeChe9Option2 = "Only NO+[PtF6]-";
    public static String threeChe9Option3 = "Only Xe[PtF6]-";
    public static String threeChe9Option4 = " Na+[PtF6]-,  NO+[PtF6]-, Xe[PtF6]-";

    public static String threeChe10 = "Which of the following are not correct about ideal solution ?";
    public static String threeChe10Option1 = "ΔH(mix) = 0";
    public static String threeChe10Option2 = "ΔG(mix) ≠ 0";
    public static String threeChe10Option3 = "ΔS(mix) = 0";
    public static String threeChe10Option4 = "ΔS(mix) ≠ 0";

    public static String threeChe11 = "Which of the following is correct for a non-ideal solution of liquids A and B, showing negative deviation";
    public static String threeChe11Option1 = "ΔH(mix) = -ve";
    public static String threeChe11Option2 = "ΔV(mix) = -ve";
    public static String threeChe11Option3 = "ΔS(mix) = +ve";
    public static String threeChe11Option4 = "ΔG(mix) = -ve";

    public static String threeChe12 = "In which of the following reaction shown in the option(s). SO₂ is acting as oxidizing agent?";
    public static String threeChe12Option1 = "H₂S + SO2 ->";
    public static String threeChe12Option2 = "FeCl3 solution + SO2 ->";
    public static String threeChe12Option3 = "FeCl2 solution + SO2 -> (in pH = 1-2)";
    public static String threeChe12Option4 = "SnCl2 solution + SO2 -> (pH = 1-2)";

    //

    public static String threeChe13 = "Which of the following properties of the metal gets changed due to formation of interstitial carbide?";
    public static String threeChe13Option1 = "Density";
    public static String threeChe13Option2 = "Hardness";
    public static String threeChe13Option3 = "Malleability";
    public static String threeChe13Option4 = "Electrical Conductivity";

    public static String threeChe14 = "Which of the following species are formed by Lewis acid base interaction?";
    public static String threeChe14Option1 = "CH4";
    public static String threeChe14Option2 = "NaBF4";
    public static String threeChe14Option3 = "PCl4+";
    public static String threeChe14Option4 = "Al(OH)4-";

    public static String threeChe15 = "Select incorrect statement(s)";
    public static String threeChe15Option1 = "Methoxy ethane give iodoethane with one equivalent anhydrous HI ";
    public static String threeChe15Option2 = "Order of basicity in aqueous medium MeNH2 < Me2NH < Me3N";
    public static String threeChe15Option3 = "Optically active alkanol will show total retaintion of configuration during its reaction with SOCl2";
    public static String threeChe15Option4 = "S-2-bromobutane gives R-2-methylbutane nitrile with AgCN as major product";

    public static String threeChe16 = "Cl2 + OH- (Hot & Conc.) -> A + B.  Find the sum of oxidation state of Cl in A and B";

    public static String threeChe17 = "Total number of stereoisomers in above compound are ";

    public static String threeChe18 = "Number of correct statement(s) is (are) ";

    public static String threeChe19 = "How many cyclic alkenes (excluding stereoisomer) will produce methyl cyclohexane on catalytic hydrogenation?";

    public static String threeChe20 = "Calculate the number of double bonds in structure of P4O10";



    // Third Mock - Maths

    public static String threeMat1 = "The differential equation representing the family of curves y² = 2C(x + √C) where C is a positive parameter, is of";
    public static String threeMat1Option1 = "Order 1, Degree 3";
    public static String threeMat1Option2 = "Order 2, Degree 3";
    public static String threeMat1Option3 = "Order 1, Degree 2";
    public static String threeMat1Option4 = "Order 1, Degree 1";

    public static String threeMat2 = "Above limit is equal to ";
    public static String threeMat2Option1 = "1";
    public static String threeMat2Option2 = "2";
    public static String threeMat2Option3 = "3";
    public static String threeMat2Option4 = "4";


    public static String threeMat3 = "If (a,0) is a point on a diameter of the circle x²+y²=4, then x²-4x-a²=0 has";
    public static String threeMat3Option1 = "exactly one real root in (-1,0]";
    public static String threeMat3Option2 = "exactly one real root in [8,11]";
    public static String threeMat3Option3 = "distinct roots less than -1";
    public static String threeMat3Option4 = "distinct roots greater than 5";

    public static String threeMat4 = "If the system of equation sinx.cos2y = (a²-1)²+1 and cosx.sin2y = (a+1) has a solution, then the value of a is";
    public static String threeMat4Option1 = "0,1";
    public static String threeMat4Option2 = "1";
    public static String threeMat4Option3 = "-1";
    public static String threeMat4Option4 = "+1,-1";

    public static String threeMat5 = "The reflection of the curve xy=1 in the line y=2x is the curve 12x²+rxy+sy²+t=0, then the value if r is";
    public static String threeMat5Option1 = "-7";
    public static String threeMat5Option2 = "25";
    public static String threeMat5Option3 = "-175";
    public static String threeMat5Option4 = "90";

    public static String threeMat6 = "The value of above expression is";
    public static String threeMat6Option1 = "4/π";
    public static String threeMat6Option2 = "3/π";
    public static String threeMat6Option3 = "2/π";
    public static String threeMat6Option4 = "6/π";

    public static String threeMat7 = "Let x and y be real numbers satisfying x²+y²=10. The greatest value of (√3+√2)x - (√3-√2)y equals to";
    public static String threeMat7Option1 = "50";
    public static String threeMat7Option2 = "10(5+2√6)";
    public static String threeMat7Option3 = "5+2√6";
    public static String threeMat7Option4 = "10";

    public static String threeMat8 = "If xsina + ysin2a + zsin3a = sin4a, xsinb + ysin2b + zsin3b = sin4b and xsinc + ysin2c + zsin3c = sin4c, then the roots of the above equation (where a,b,c ≠ nπ) are";
    public static String threeMat8Option1 = "sina, sinb, sinc";
    public static String threeMat8Option2 = "cosa, cosb, cosc";
    public static String threeMat8Option3 = "sin2a, sin2b, sin2c";
    public static String threeMat8Option4 = "cos2a, cos2b, cos2c";

    public static String threeMat9 = "If a vector r is equally inclined with the vectors a = cosx ^i + sinx ^j, b = -sinx ^i + cosx ^j and c = k, then the cosine of angle between r and a is";
    public static String threeMat9Option1 = "1/√2";
    public static String threeMat9Option2 = "1/√3";
    public static String threeMat9Option3 = "1/3";
    public static String threeMat9Option4 = "1/2";

    public static String threeMat10 = "If the parabola y = (a-b)x² + (b-c)x + (c-a) touches the x-axis in the interval (0,2), then the line ax + by + c = 0";
    public static String threeMat10Option1 = "always passes though a fixed point (-2,1)";
    public static String threeMat10Option2 = "always passes though a fixed point (2,-1)";
    public static String threeMat10Option3 = "is always tangent to the circle x²+y² = (a+b-c)²";
    public static String threeMat10Option4 = "represents a family of parallel lines";

    // finfinity
    public static String threeMat11 = "If f'(x) > f(x) for all x >= 1 anf f(1)=0, then";
    public static String threeMat11Option1 = "(e^x).f(x) is a decreasing function";
    public static String threeMat11Option2 = "(e^-x).f(x) is an increasing function";
    public static String threeMat11Option3 = "f(x) > 0 for all x belongs to (1,∞)";
    public static String threeMat11Option4 = "f(x) < 0 for all x belongs to [1,∞)";

    public static String threeMat12 = "f:R->R; f(x) is continuous function satisfying f(x)+f(x²)=2 for all x belongs to R, then f(x) is";
    public static String threeMat12Option1 = "into";
    public static String threeMat12Option2 = "many-one";
    public static String threeMat12Option3 = "constant";
    public static String threeMat12Option4 = "periodic";

    //

    public static String threeMat13 = "If z1, z2 and z3 are any three unimodular complex numbers which satisfy the above equation, then the value of |z1+z2+z3| is equal to";
    public static String threeMat13Option1 = "4";
    public static String threeMat13Option2 = "3";
    public static String threeMat13Option3 = "2";
    public static String threeMat13Option4 = "1";

    public static String threeMat14 = "Let f: R+ -> R be a function which satisfies the above equation, then f(x) can be equal to ";
    public static String threeMat14Option1 = "(2x+1)/x";
    public static String threeMat14Option2 = "-(x+1)/x";
    public static String threeMat14Option3 = "(5x+2)/3x";
    public static String threeMat14Option4 = "-2(2x+1)/3x";

    public static String threeMat15 = "The tangent at any point P on a standard eclipse with foci as S and S' meets the tangents at the vertices A and A' in the points V and V' respectively, then";
    public static String threeMat15Option1 = "(AV).(A'V') = b²";
    public static String threeMat15Option2 = "(AV).(A'V') = a²";
    public static String threeMat15Option3 = "∠V'SV = 90°";
    public static String threeMat15Option4 = "V'S'VS is a cyclic quadrilateral";

    public static String threeMat16 = "The value of above limit is 11k where k is..";

    public static String threeMat17 = "Number of solution to above equation is (if x belongs to [0,4π])";

    public static String threeMat18 = "The minimum distance of the point (1,1,1) from the plane x+y+z=1 measured parallel to the line (x-2) = (y-3)/2 = (z-4)/3 is k, where 3k/√14";

    public static String threeMat19 = "The minimum number of normals that can always be drawn to an eclipse/hyperbola is";

    public static String threeMat20 = "The number of integral values of a; a belongs to (6,100) for which the equation [tan]²+tanx-a=0 has real roots; where [.] denotes the greatest integer function is";


    public static int oneChe1A = 2;
    public static int oneChe2A = 4;
    public static int oneChe3A = 3;
    public static int oneChe4A = 2;
    public static int oneChe5A = 2;
    public static int oneChe6A = 3;
    public static int oneChe7A = 4;
    public static int oneChe8A = 4;
    public static int oneChe9A = 1;
    public static int oneChe10A =2 ;
    public static ArrayList<Integer> oneChe11A = new ArrayList<>(Arrays.asList(1,2,3));
    public static ArrayList<Integer> oneChe12A = new ArrayList<>(Arrays.asList(1,2,4));
    public static ArrayList<Integer> oneChe13A = new ArrayList<>(Arrays.asList(1,3));
    public static ArrayList<Integer> oneChe14A = new ArrayList<>(Arrays.asList(1,3));
    public static ArrayList<Integer> oneChe15A = new ArrayList<>(Arrays.asList(1,2,4));
    public static int oneChe16A = 4;
    public static int oneChe17A = 14;
    public static int oneChe18A = 4;
    public static int oneChe19A = 4;
    public static int oneChe20A = 1;


    public static int oneMat1A = 2;
    public static int oneMat2A = 1;
    public static int oneMat3A = 1;
    public static int oneMat4A = 3;
    public static int oneMat5A = 4;
    public static int oneMat6A = 4;
    public static int oneMat7A = 3;
    public static int oneMat8A = 2;
    public static int oneMat9A = 3;
    public static int oneMat10A =2 ;
    public static ArrayList<Integer> oneMat11A = new ArrayList<>(Arrays.asList(1,2,3,4));
    public static ArrayList<Integer> oneMat12A = new ArrayList<>(Arrays.asList(2,4));
    public static ArrayList<Integer> oneMat13A = new ArrayList<>(Arrays.asList(1,2,3));
    public static ArrayList<Integer> oneMat14A = new ArrayList<>(Arrays.asList(1,3));
    public static ArrayList<Integer> oneMat15A = new ArrayList<>(Arrays.asList(3,4));
    public static int oneMat16A = 2;
    public static int oneMat17A = 1;
    public static int oneMat18A = 0;
    public static int oneMat19A = 5;
    public static int oneMat20A = 1;


    public static int twoPhy1A = 2;
    public static int twoPhy2A = 2;
    public static int twoPhy3A = 3;
    public static int twoPhy4A = 2;
    public static int twoPhy5A = 1;
    public static int twoPhy6A = 1;
    public static int twoPhy7A = 1;
    public static int twoPhy8A = 1;
    public static int twoPhy9A = 4;
    public static int twoPhy10A =2 ;
    public static ArrayList<Integer> twoPhy11A = new ArrayList<>(Arrays.asList(2,4));
    public static ArrayList<Integer> twoPhy12A = new ArrayList<>(Arrays.asList(2,3));
    public static ArrayList<Integer> twoPhy13A = new ArrayList<>(Arrays.asList(2,3,4));
    public static ArrayList<Integer> twoPhy14A = new ArrayList<>(Arrays.asList(1,2,3));
    public static ArrayList<Integer> twoPhy15A = new ArrayList<>(Arrays.asList(2,4));
    public static int twoPhy16A = 150;
    public static int twoPhy17A = 1;
    public static int twoPhy18A = 180;
    public static int twoPhy19A = 0;
    public static int twoPhy20A = 9;

    public static int twoChe1A = 2;
    public static int twoChe2A = 4;
    public static int twoChe3A = 2;
    public static int twoChe4A = 1;
    public static int twoChe5A = 2;
    public static int twoChe6A = 2;
    public static int twoChe7A = 4;
    public static int twoChe8A = 3;
    public static int twoChe9A = 3;
    public static int twoChe10A =2 ;
    public static ArrayList<Integer> twoChe11A = new ArrayList<>(Arrays.asList(2,3));
    public static ArrayList<Integer> twoChe12A = new ArrayList<>(Arrays.asList(2));
    public static ArrayList<Integer> twoChe13A = new ArrayList<>(Arrays.asList(3,4));
    public static ArrayList<Integer> twoChe14A = new ArrayList<>(Arrays.asList(1,2,4));
    public static ArrayList<Integer> twoChe15A = new ArrayList<>(Arrays.asList(1,2,3));
    public static int twoChe16A = 1;
    public static int twoChe17A = 1;
    public static int twoChe18A = 3;
    public static int twoChe19A = 6;
    public static int twoChe20A = 2;

    public static int twoMat1A = 1;
    public static int twoMat2A = 4;
    public static int twoMat3A = 2;
    public static int twoMat4A = 3;
    public static int twoMat5A = 4;
    public static int twoMat6A = 2;
    public static int twoMat7A = 3;
    public static int twoMat8A = 1;
    public static int twoMat9A = 4;
    public static int twoMat10A =3 ;
    public static ArrayList<Integer> twoMat11A = new ArrayList<>(Arrays.asList(1,4));
    public static ArrayList<Integer> twoMat12A = new ArrayList<>(Arrays.asList(2,4));
    public static ArrayList<Integer> twoMat13A = new ArrayList<>(Arrays.asList(1,2,3,4));
    public static ArrayList<Integer> twoMat14A = new ArrayList<>(Arrays.asList(2,4));
    public static ArrayList<Integer> twoMat15A = new ArrayList<>(Arrays.asList(2,3));
    public static int twoMat16A = 3;
    public static int twoMat17A = 5;
    public static int twoMat18A = 5;
    public static int twoMat19A = 1;
    public static int twoMat20A = 2;

    public static int threePhy1A = 1;
    public static int threePhy2A = 3;
    public static int threePhy3A = 1;
    public static int threePhy4A = 2;
    public static int threePhy5A = 2;
    public static int threePhy6A = 1;
    public static int threePhy7A = 2;
    public static int threePhy8A = 2;
    public static int threePhy9A = 1;
    public static int threePhy10A =3 ;
    public static ArrayList<Integer> threePhy11A = new ArrayList<>(Arrays.asList(2,3,4));
    public static ArrayList<Integer> threePhy12A = new ArrayList<>(Arrays.asList(2,3));
    public static ArrayList<Integer> threePhy13A = new ArrayList<>(Arrays.asList(1,2,3,4));
    public static ArrayList<Integer> threePhy14A = new ArrayList<>(Arrays.asList(2,3,4));
    public static ArrayList<Integer> threePhy15A = new ArrayList<>(Arrays.asList(1,2,3,4));
    public static int threePhy16A = 4;
    public static int threePhy17A = 1;
    public static int threePhy18A = 0;
    public static int threePhy19A = 3;
    public static int threePhy20A = 5;

    public static int threeChe1A = 3;
    public static int threeChe2A = 3;
    public static int threeChe3A = 2;
    public static int threeChe4A = 2;
    public static int threeChe5A = 2;
    public static int threeChe6A = 3;
    public static int threeChe7A = 3;
    public static int threeChe8A = 3;
    public static int threeChe9A = 4;
    public static int threeChe10A =3 ;
    public static ArrayList<Integer> threeChe11A = new ArrayList<>(Arrays.asList(1,2,3,4));
    public static ArrayList<Integer> threeChe12A = new ArrayList<>(Arrays.asList(1,3,4));
    public static ArrayList<Integer> threeChe13A = new ArrayList<>(Arrays.asList(1,2,3));
    public static ArrayList<Integer> threeChe14A = new ArrayList<>(Arrays.asList(2,4));
    public static ArrayList<Integer> threeChe15A = new ArrayList<>(Arrays.asList(1,2,4));
    public static int threeChe16A = 4;
    public static int threeChe17A = 4;
    public static int threeChe18A = 3;
    public static int threeChe19A = 4;
    public static int threeChe20A = 4;

    public static int threeMat1A = 1;
    public static int threeMat2A = 2;
    public static int threeMat3A = 1;
    public static int threeMat4A = 3;
    public static int threeMat5A = 1;
    public static int threeMat6A = 3;
    public static int threeMat7A = 4;
    public static int threeMat8A = 2;
    public static int threeMat9A = 2;
    public static int threeMat10A =1 ;
    public static ArrayList<Integer> threeMat11A = new ArrayList<>(Arrays.asList(2,3));
    public static ArrayList<Integer> threeMat12A = new ArrayList<>(Arrays.asList(1,2,3,4));
    public static ArrayList<Integer> threeMat13A = new ArrayList<>(Arrays.asList(3,4));
    public static ArrayList<Integer> threeMat14A = new ArrayList<>(Arrays.asList(1,4));
    public static ArrayList<Integer> threeMat15A = new ArrayList<>(Arrays.asList(1,3,4));
    public static int threeMat16A = 2;
    public static int threeMat17A = 4;
    public static int threeMat18A = 1;
    public static int threeMat19A = 2;
    public static int threeMat20A = 7;



}

