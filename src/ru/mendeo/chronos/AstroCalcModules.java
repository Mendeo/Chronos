/* Этот код написан в марте 2012 года Меняйло Александром Николаевичем.
 * Данный класс является набором статических методов, используемых для различных вычислений, а именно: 
 * Сортировка массивов методом быстрой сортировки.
 * Астрономические расчёты:
 * В астрономических расчётах используются формулы из
 * Astronomical Algorithms, first edition, by Jean Meeus Published by Willmann-Bell, Inc. 1991
 * Для вычисления разницы между универсальным временем UT и динамическим (приближённо эфемеридным)
 * используются формулы с сайта NASA (http://eclipse.gsfc.nasa.gov/SEhelp/deltatpoly2004.html)
 * с коэффициентами преобразованными в Юлианские дни.
 * Погрешности этих формул можно найти тут: http://eclipse.gsfc.nasa.gov/SEhelp/uncertainty2004.html.
 * Ими и определеяется диапазон годов для которых рассчитываются астрономические явления (-1500 +3000).
 * Стандартное отклонение разницы UT-ET в крайних точках ~1900 сек.
 */
package ru.mendeo.chronos;

import java.util.Calendar;

public class AstroCalcModules
{
	public static final int OBJECT_MOON = 11;
	public static final int OBJECT_SUN = 12;
	public static final int EVENT_RISE = 21;
	public static final int EVENT_TRANSIT = 22;
	public static final int EVENT_SETTING = 23;
	public static final int NEW_MOON = 1;
	public static final int FIRST_QUARTER = 2;
	public static final int FULL_MOON = 3;
	public static final int LAST_QUARTER = 4;
	public static final double RPD = Math.PI / 180;
	public static final double DOUBLE_PI = Math.PI + Math.PI;	
	public static final double HALF_PI = Math.PI / 2.0;
	//Переменные, необходимые для хранения основных фаз луны.
	private static int mCurrentMonth = -1;
	private static int mCurrentYear = -1;
	private static double[] mCurrentMonthDaysOfMainMoonPhases;
	private static int[] mCurrentMonthTypesOfMainMoonPhases;
	//Переменные, необходимые для хранения времени восхода, зенита и заката.
	//Здесь первые три элемента соответствуют Солнцу (восход, зенит, закат), а вторые три - Луне.
	//Храним номер дня в году.
	private static int[] mObjectEventDaysOfYear = {-1, -1, -1, -1, -1, -1};
	private static int[] mObjectEventYear = {-1, -1, -1, -1, -1, -1};
	//Храним координаты
	private static double[] mObjectEventLongitude = {-1.0, -1.0, -1.0, -1.0, -1.0, -1.0};
	private static double[] mObjectEventLatitude = {-1.0, -1.0, -1.0, -1.0, -1.0, -1.0};
	//Храним время события.
	private static Calendar[] mObjectEventTimes = {Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()};
	//Храним так же максимальную высоту небесных тел в Зените. (Нужно для отрисовки кнопок и для вывода на экран этих данных).
	//Первый элемент - высота Солнца, второй - Луны.
	private static double[] mObjectTransitHeight = {-1.0, -1.0};
	//Храним координаты, в которых измереяется высота Солнца и Луны (первый элемент для Солнца, второй для Луны).
	private static double[] mOblectTransitLongitude = {-1.0, -1.0};
	private static double[] mOblectTransitLatitude = {-1.0, -1.0};
	//Храним год и номер дня в году. (Первые два элемента соответствуют Солнцу, вторые два - Луне).
	private static int[] mObjectTransitHeightDate = {-1, -1, -1, -1};
	//Храним закатную/восходную высоты Солнца и Луны. А так же дату, на которую рассчитывается эта высота для Луны.
	private static double mSunHorisontHeight = RPD * (-50.0 / 60.0);
	private static double mMoonHorisontHeight = 0.0;
	private static int mDateOfMoonHorisontHeight = -1;
	//private static double mLongitude = 0.0;
	//private static double mLatitude = 0.0;
	
	//***********************************************************
	//РАСЧЁТ ОСВЕЩЁННОЙ ДОЛИ ЛУНЫ И ЛУННОЙ ФАЗЫ
	//Расчёт для заданного месяца и года (по UTC) одной из 4-х основных фаз луны (новолуния, или первой четверти, или полнолуния, или последней четверти).
	//Метод возвращает массив, содержащий вещественные значения дней, в которых произойдёт искомое событие. По дробной части в дальнейшем вычисляется точное время события.
	private static double[] GetDaysOfMoonMainPhases(GregorianDate GD, int Phase)
	{
		boolean LeapYear = IsLeapYear(GD.Year);
		double DaysInWholeYear = LeapYear ? 366.0 : 365.0;
		double k = LeapYear ? 1.0 : 2.0;
		double DaysAfterNewYear = Math.floor(275.0 * (double)GD.Month / 9.0) - k * Math.floor(((double)GD.Month + 9.0) / 12.0) + (GetNumberOfDaysInMonth(GD.Year, GD.Month) / 2.0) - 30.0;		
		//Вещественный год.
		double RealYear = (double)GD.Year + DaysAfterNewYear / DaysInWholeYear;
		k = (RealYear - 2000.0) * 12.3685;
		k = (k >= 0) ? (Math.floor(k) - 1.0) : (Math.ceil(k) - 1.0);
		switch (Phase)
		{
			case NEW_MOON:
				//Ничего не делаем.
			break;
			case FIRST_QUARTER:
				k += 0.25;
			break;
			case FULL_MOON:
				k += 0.5;
			break;
			case LAST_QUARTER:
				k += 0.75;
			break;
			default:
				Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error. Incorrect phase of moon");
				return null;
		}
		double[] DaysToOut = new double[2];
		int n = 0;
		while (true)
		{
			double T = k / 1236.85;
			double T2 = T * T;
			double T3 = T2 * T;
			double T4 = T3 * T;
			//Вычисляем Юлианский день:
			double JDE = 2451550.09765 + 29.530588853 * k + 0.0001337 * T2 - 0.000000150 * T3 + 0.00000000073 * T4;
			//Далее расчёт поправочных коэффициентов для JDE:
			double E = 1 - 0.002516 * T - 0.0000074 * T2;
			double E2 = E * E;
			//Sun's mean anomaly:
			double M = RPD * (2.5534 + 29.10535669 * k - 0.0000218 * T2 - 0.00000011 * T3);
			//Moon's mean anomaly:
			double M1 = RPD * (201.5643 + 385.81693528 * k + 0.0107438 * T2 + 0.00001239 * T3 - 0.000000058 * T4);
			//Moon's argument of latitude:
			double F = RPD * (160.7108 + 390.67050274 * k - 0.0016341 * T2 - 0.00000227 * T3 + 0.000000011 * T4);
			//Latitude of the ascending node of the lunar orbit:
			double Omega = RPD * (124.7746 - 1.56375580 * k + 0.0020691 * T2 + 0.00000215 * T3);
			double DM = M + M;
			double DM1 = M1 + M1;
			double TM1 = DM1 + M1;
			double DM1M = M1 - M;
			double SM1M = M + M1;
			double DF = F + F;
			//Planetary argiments:
			double A1 = RPD * (299.77 + 0.107408 * k - 0.009173 * T2);
			double A2 = RPD * (251.88 + 0.016321 * k);
			double A3 = RPD * (251.83 + 26.651886 * k);
			double A4 = RPD * (349.42 + 36.412478 * k);
			double A5 = RPD * (84.66 + 18.206239 * k);
			double A6 = RPD * (141.74 + 53.303771 * k);
			double A7 = RPD * (207.14 + 2.453732 * k);
			double A8 = RPD * (154.84 + 7.306860 * k);
			double A9 = RPD * (34.52 + 27.261239 * k);
			double A10 = RPD * (207.19 + 0.121824 * k);
			double A11 = RPD * (291.34 + 1.844379 * k);
			double A12 = RPD * (161.72 + 24.198154 * k);
			double A13 = RPD * (239.56 + 25.513099 * k);
			double A14 = RPD * (331.55 + 3.592518 * k);
			//Поправляем JDE на поправочные коэффициенты.
			switch (Phase)
			{
				case NEW_MOON: 
					JDE += (-0.40720 * Math.sin(M1) + 0.17241 * E * Math.sin(M) + 0.01608 * Math.sin(DM1) + 0.01039 * Math.sin(DF) + 0.00739 * E * Math.sin(DM1M) - 0.00514 * E * Math.sin(SM1M) + 0.00208 * E2 * Math.sin(DM) - 0.00111 * Math.sin(M1 - DF) - 0.00057 * Math.sin(M1 + DF) + 0.00056 * E * Math.sin(DM1 + M) - 0.00042 * Math.sin(TM1) + 0.00042 * E * Math.sin(M + DF) + 0.00038 * E * Math.sin(M - DF) - 0.00024 * E * Math.sin(DM1 - M) - 0.00017 * Math.sin(Omega) - 0.00007 * Math.sin(M1 + DM) + 0.00004 * Math.sin(DM1 - DF) + 0.00004 * Math.sin(DM + M) + 0.00003 * Math.sin(SM1M - DF) + 0.00003 * Math.sin(DM1 + DF) - 0.00003 * Math.sin(SM1M + DF) + 0.00003 * Math.sin(DM1M + DF) - 0.00002 * Math.sin(DM1M - DF) - 0.00002 * Math.sin(TM1 + M) + 0.00002 * Math.sin(TM1 + M1));
				break;
				case FULL_MOON:
					JDE += (-0.40614 * Math.sin(M1) + 0.17302 * E * Math.sin(M) + 0.01614 * Math.sin(DM1) + 0.01043 * Math.sin(DF) + 0.00734 * E * Math.sin(DM1M) - 0.00515 * E * Math.sin(SM1M) + 0.00209 * E2 * Math.sin(DM) - 0.00111 * Math.sin(M1 - DF) - 0.00057 * Math.sin(M1 + DF) + 0.00056 * E * Math.sin(DM1 + M) - 0.00042 * Math.sin(TM1) + 0.00042 * E * Math.sin(M + DF) + 0.00038 * E * Math.sin(M - DF) - 0.00024 * E * Math.sin(DM1 - M) - 0.00017 * Math.sin(Omega) - 0.00007 * Math.sin(M1 + DM) + 0.00004 * Math.sin(DM1 - DF) + 0.00004 * Math.sin(DM + M) + 0.00003 * Math.sin(SM1M - DF) + 0.00003 * Math.sin(DM1 + DF) - 0.00003 * Math.sin(SM1M + DF) + 0.00003 * Math.sin(DM1M + DF) - 0.00002 * Math.sin(DM1M - DF) - 0.00002 * Math.sin(TM1 + M) + 0.00002 * Math.sin(TM1 + M1));
				break;
				case FIRST_QUARTER: case LAST_QUARTER:
					JDE += (-0.62801 * Math.sin(M1) + 0.17172 * E * Math.sin(M) - 0.01183 * E * Math.sin(SM1M) + 0.00862 * Math.sin(DM1) + 0.00804 * Math.sin(DF) + 0.00454 * E * Math.sin(DM1M) + 0.00204 * E2 * Math.sin(DM) - 0.00180 * Math.sin(M1 - DF) - 0.00070 * Math.sin(M1 + DF) - 0.00040 * Math.sin(TM1) - 0.00034 * E * Math.sin(DM1 - M) + 0.00032 * E * Math.sin(M + DF) + 0.00032 * E * Math.sin(M - DF) - 0.00028 * E2 * Math.sin(M1 + DM) + 0.00027 * E * Math.sin(DM1 + M) - 0.00017 * Math.sin(Omega) - 0.00005 * Math.sin(DM1M - DF) + 0.00004 * Math.sin(DM1 + DF) - 0.00004 * Math.sin(SM1M + DF) + 0.00004 * Math.sin(M1 - DM) + 0.00003 * Math.sin(SM1M - DF) + 0.00003 * Math.sin(DM + M) + 0.00002 * Math.sin(DM1 - DF) + 0.00002 * Math.sin(DM1M + DF) - 0.00002 * Math.sin(TM1 + M));
					double W = 0.00306 - 0.00038 * E * Math.cos(M) + 0.00026 * Math.cos(M1) - 0.00002 * Math.cos(DM1M) + 0.00002 * Math.cos(SM1M) + 0.00002 * Math.cos(DF);
					if (Phase == FIRST_QUARTER)
					{
						JDE += W; 
					}
					else
					{
						JDE -= W;
					}
				break;
			}
			JDE += (0.000325 * Math.sin(A1) + 0.000165 * Math.sin(A2) + 0.000164 * Math.sin(A3) + 0.000126 * Math.sin(A4) + 0.000110 * Math.sin(A5) + 0.000062 * Math.sin(A6) + 0.000060 * Math.sin(A7) + 0.000056 * Math.sin(A8) + 0.000047 * Math.sin(A9) + 0.000042 * Math.sin(A10) + 0.000040 * Math.sin(A11) + 0.000037 * Math.sin(A12) + 0.000035 * Math.sin(A13) + 0.000023 * Math.sin(A14));
			//Вычисляем по JD Григорианский день
			GregorianDate GD_UTC_Phase = JulianDayToGregorianDate(JDE, true);
			if ((GD_UTC_Phase.Month > GD.Month && GD_UTC_Phase.Year == GD.Year) || GD_UTC_Phase.Year > GD.Year)
			{
				break;
			}
			else if (GD_UTC_Phase.Month == GD.Month)
			{
				DaysToOut[n] = GD_UTC_Phase.RealDay;
				n++;
			}
			k += 1.0;
		}
		if (n == 0)
		{
			return null;
		}
		else if (n != 2)
		{
			double tmp = DaysToOut[0];
			DaysToOut = new double[1];
			DaysToOut[0] = tmp;
		}
		return DaysToOut;
	}
	//Публичный метод для определения основных фаз луны в месяце, заданном календарём.
	public static Calendar[] GetMoonMoonMainPhases(Calendar cal, int Phase)
	{
		GregorianDate GD = GetGregorianDateFromCalendar(cal, false);
		double[] PhaseDays = GetDaysOfMoonMainPhases(GD, Phase);
		if (PhaseDays == null) return null;
		int size = PhaseDays.length;
		GregorianDate GD_Phase = new GregorianDate(GD.Year, GD.Month, GD.RealDay);
		Calendar[] OutCals = new Calendar[size];
		for (int i = 0; i < size; i++)
		{
			GD_Phase.Year = GD.Year;
			GD_Phase.Month = GD.Month;
			GD_Phase.RealDay = PhaseDays[i];
			OutCals[i] = GetCalendarFromGregorianDate(GD_Phase, true);
		}
		return OutCals;
	}
	//Расчёт видимости луны в процентах на заданный момент в GregorianDate, в котором время дано по UTC.
	public static double GetMoonVisiblePercents(Calendar CurTime)
	{
		//Создаём дату в нашем формате (GregorianDate) и конвертируем её в UTC время.
		GregorianDate GD_UTC = GetGregorianDateFromCalendar(CurTime, true);
		if (!CheckCregorianDate(GD_UTC)) return -1.0;
		double JDE = GregorianDateToJulianDay(GD_UTC, true);
		double T = (JDE - 2451545.0) / 36525.0;
		double T2 = T * T;
		double T3 = T2 * T;
		double T4 = T3 * T;
		//Sun's mean anomaly: 
		double M = RPD * (357.5291092 + 35999.0502909 * T - 0.0001536 * T2 + T3 / 24490000);
		//Moon's mean anomaly: 
		double M1 = RPD * (134.9634114 + 477198.8676313 * T + 0.0089970 * T2 + T3 / 69699 - T4 / 14712000);
		//Moon's mean e1ongation : 
		double D = RPD * (297.8502042 + 445267.1115168 * T - 0.0016300 * T2 + T3 / 545868 - T4 / 11306500);
		double DubD = D + D;
		//Phase angle:
		double I = Math.PI - D + RPD * (-6.289 * Math.sin(M1) + 2.100 * Math.sin(M) - 1.274 * Math.sin(DubD - M1) - 0.658 * Math.sin(DubD) - 0.214 * Math.sin(M1 + M1) - 0.110 * Math.sin(D));
		return (1 + Math.cos(I)) * 50.0;
	}
	//Прверка является ли данный год високосным
	private static boolean IsLeapYear(int Year)
	{
		if (Year % 4 == 0)
		{
			if (Year < 1582)
			{
				return true;
			}
			else
			{
				if (Year % 400 == 0)
				{
					return false;
				}
				else
				{
					return true;
				}
				
			}
		}
		else
		{
			return false;
		}
	}
	//Создание массивов (если они не созданы), содержащих основные фазы луны на заданный месяц и год.
	//Это необходимо для того, чтобы не рассчитывать фазы луны многократно. 
	private static void CheckOrCreateMoonMainPhasesArrays(GregorianDate GD_UTC)
	{
		if (mCurrentMonth != GD_UTC.Month || mCurrentYear != GD_UTC.Year)
		{
			Log.i(PublicConstantsAndMethods.MY_LOG_TAG, "Moon main phases arrays start filling...");
			mCurrentYear = GD_UTC.Year;
			mCurrentMonth = GD_UTC.Month; 
			double[] NewMoon = GetDaysOfMoonMainPhases(GD_UTC, NEW_MOON);
			double[] FirstQuater = GetDaysOfMoonMainPhases(GD_UTC, FIRST_QUARTER);
			double[] FullMoon = GetDaysOfMoonMainPhases(GD_UTC, FULL_MOON);
			double[] LastQuater = GetDaysOfMoonMainPhases(GD_UTC, LAST_QUARTER);
			int size = 0;
			if (NewMoon != null) size += NewMoon.length;
			if (FirstQuater != null) size += FirstQuater.length;
			if (FullMoon != null) size += FullMoon.length;
			if (LastQuater != null) size += LastQuater.length;
			mCurrentMonthDaysOfMainMoonPhases = new double[size];
			mCurrentMonthTypesOfMainMoonPhases = new int[size];
			//Загоняем все дни в один массив
			int j = 0;
			int i = 0;
			if (NewMoon != null)
			{
				for (i = 0; i < NewMoon.length; i++)
				{
					mCurrentMonthDaysOfMainMoonPhases[j] = NewMoon[i];
					mCurrentMonthTypesOfMainMoonPhases[j] = NEW_MOON;
					j++;
				}
			}
			if (FirstQuater != null)
			{
				for (i = 0; i < FirstQuater.length; i++)
				{
					mCurrentMonthDaysOfMainMoonPhases[j] = FirstQuater[i];
					mCurrentMonthTypesOfMainMoonPhases[j] = FIRST_QUARTER;
					j++;
				}
			}
			if (FullMoon != null)
			{
				for (i = 0; i < FullMoon.length; i++)
				{
					mCurrentMonthDaysOfMainMoonPhases[j] = FullMoon[i];
					mCurrentMonthTypesOfMainMoonPhases[j] = FULL_MOON;
					j++;
				}
			}
			if (LastQuater != null)
			{
				for (i = 0; i < LastQuater.length; i++)
				{
					mCurrentMonthDaysOfMainMoonPhases[j] = LastQuater[i];
					mCurrentMonthTypesOfMainMoonPhases[j] = LAST_QUARTER;
					j++;
				}
			}
		}
	}
	//Определение фазы луны по вычисленным основным лунным фазам, которые сохранены в статических массивах
	//Основные фазы луны определяются по UTC времени, но отображаются в течение суток по локальному.
	public static int GetMoonPhasePreserve(Calendar CurTime)
	{
		//Создаём дату в нашем формате (GregorianDate) и конвертируем её в UTC время.
		GregorianDate GD_UTC = GetGregorianDateFromCalendar(CurTime, true);
		if (!CheckCregorianDate(GD_UTC)) return -1;
		CheckOrCreateMoonMainPhasesArrays(GD_UTC);
		//Массивы созданы. Определяем фазу луны.
		double MinDelta = Double.MAX_VALUE;
		double Delta = -1.0;
		int CloslyPhaseNumber = -1;
		//Вычисляем к каким основным фазам луны близок текущий день.
		for (int i = 0; i < mCurrentMonthDaysOfMainMoonPhases.length; i++)
		{
			Delta = GD_UTC.RealDay - mCurrentMonthDaysOfMainMoonPhases[i];
			if (Math.abs(Delta) < Math.abs(MinDelta))
			{
				MinDelta = Delta;
				CloslyPhaseNumber = i;
			}
		}
		int Phase = -1;
		GD_UTC.RealDay = mCurrentMonthDaysOfMainMoonPhases[CloslyPhaseNumber];
		//Получаем календарь с временем ближайшей основной фазы по локальному времени.
		Calendar PhaseTime = GetCalendarFromGregorianDate(GD_UTC, true);
		switch (mCurrentMonthTypesOfMainMoonPhases[CloslyPhaseNumber])
		{
			case NEW_MOON:
				if (CurTime.get(Calendar.DAY_OF_MONTH) == PhaseTime.get(Calendar.DAY_OF_MONTH))
				{
					Phase = 1;
				}
				else if (MinDelta > 0.0)
				{
					Phase = 2;
				}
				else
				{
					Phase = 8;
				}
			break;
			case FIRST_QUARTER:
				if (CurTime.get(Calendar.DAY_OF_MONTH) == PhaseTime.get(Calendar.DAY_OF_MONTH))
				{
					Phase = 3;
				}
				else if (MinDelta > 0.0)
				{
					Phase = 4;
				}
				else
				{
					Phase = 2;
				}
			break;
			case FULL_MOON:
				if (CurTime.get(Calendar.DAY_OF_MONTH) == PhaseTime.get(Calendar.DAY_OF_MONTH))
				{
					Phase = 5;
				}
				else if (MinDelta > 0.0)
				{
					Phase = 6;
				}
				else
				{
					Phase = 4;
				}
			break;
			case LAST_QUARTER:
				if (CurTime.get(Calendar.DAY_OF_MONTH) == PhaseTime.get(Calendar.DAY_OF_MONTH))
				{
					Phase = 7;
				}
				else if (MinDelta > 0.0)
				{
					Phase = 8;
				}
				else
				{
					Phase = 6;
				}
			break;
		}
		return Phase;
	}
	//Расчёт положения Солнца и луны
	//Вычисляем экваториальные координаты Солнца. Первый элемент массива - прямое восхождение, второй - склонение.
	//В данном случае вычисляются средние кординаты (не учитывающие нутацию и остальное).
	//Метод возвращает значения в радианах!
	private static double[] GetSolarCoordinates(double JDE)
	{
		double T = (JDE - 2451545.0) / 36525.0;
		double T2 = T * T;
		double T3 = T2 * T;
		//The geometric mean longitude of the Sun, referred to the mean equinox of the date:
		double L0 = RPD * (280.46645 + 36000.76983 * T + 0.0003032 * T2);
		//The Sun's mean anomaly:
		double M = RPD * (357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T3);
		//Sun's equation of the center:
		double C = RPD * ((1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(M) + (0.019993 - 0.000101 * T) * Math.sin(M + M) + 0.000290 * Math.sin(M + M + M));
		//The the Sun's true longitude:
		//Эклиптическая долгода Солнца.
		double Teta = L0 + C;		
		//Принимаем эклиптическую широту Солнца равной 0, т.к. согласно Меюсу она никогда не превышает 1.2 секунды.
		//Расчитываем экваториальные координаты:
		//The obliquity of the ecliptic
		double Epsilon = RPD * (23.43929111111111 - 0.01300416666666667 * T - 0.00000016388888889 * T2 + 0.00000050361111111 * T3);
		double Alpha = Math.atan2(Math.cos(Epsilon) * Math.sin(Teta), Math.cos(Teta));
		double Delta = Math.asin(Math.sin(Epsilon) * Math.sin(Teta));
		double[] Coords = new double[2];
		Coords[0] = Alpha;
		Coords[1] = Delta;
		return Coords;
	}
	//Вычисляем экваториальные координаты Луны или расстояние до Луны и горизонтальный паралакс.
	//В первом случае: первый элемент массива - прямое восхождение, второй - склонение.
	//Во втором: первый элемент - расстояние до Луны, второй - equatorial horizontal parallax.
	//Погрешность расчёта около 10 секунд, т.е. 1/360 градуса.
	//В данном случае вычисляются средние кординаты (не учитывающие нутацию и остальное).
	//Всё сделано в одном методе, т.к. для рассчёта применяются одни и теже коэффициенты.
	private static double[] GetMoonCoordinatesOrDistance(double JDE, boolean CalcCoords)
	{
		double T = (JDE - 2451545.0) / 36525.0;
		double T2 = T * T;
		double T3 = T2 * T;
		double T4 = T3 * T;
		//The the Moon's mean longitude:
		double L1 = RPD * (218.3164591 + 481267.88134236 * T - 0.0013268 * T2 + T3 / 538841.0 - T4 / 65194000.0);
		//Moon's mean e1ongation : 
		double D = RPD * (297.8502042 + 445267.1115168 * T - 0.0016300 * T2 + T3 / 545868.0 - T4 / 113065000.0);
		double DD = D + D;
		double TD = DD + D;
		double FD = TD + D;
		//Sun's mean anomaly: 
		double M = RPD * (357.5291092 + 35999.0502909 * T - 0.0001536 * T2 + T3 / 24490000.0);
		double DM = M + M;
		//Moon's mean anomaly: 
		double M1 = RPD * (134.9634114 + 477198.8676313 * T + 0.0089970 * T2 + T3 / 69699.0 - T4 / 14712000.0);
		double DM1 = M1 + M1;
		double TM1 = DM1 + M1;
		//Moon's argument of latitude (mean distance of the Moon from its ascending node)
		double F = RPD * (93.2720993 + 483202.0175273 * T - 0.0034029 * T2 - T3 / 3526000.0 + T4 / 863310000.0);
		double DF = F + F;
		double TF = DF + F;
		double A1 = RPD * (119.75 + 131.849 * T);
		double A2 = RPD * (53.09 + 479264.290 * T);
		double A3 = RPD * (313.45 + 481266.484 * T);
		double E = 1 - 0.002516 * T - 0.0000074 * T2;
		double E2 = E * E;
		//Аргументы синуса и косинуса из таблицы 45.A в Меюсе.
		double[] Args = {M1, DD - M1, DD, DM1, M, DF, DD - DM1, DD - M - M1, DD + M1, DD - M, M - M1, D, M + M1, DD - DF, M1 + DF, M1 - DF, FD - M1, TM1, FD - DM1, DD + M - M1, DD + M, D - M1, D + M, DD - M + M1, DD + DM1, FD, DD - TM1, M - DM1, DD - M1 + DF, DD - M - DM1, D + M1, DD - DM, M + DM1, DM, DD - DM - M1, DD + M1 - DF, DD + DF, FD - M - M1, DM1 + DF, TD - M1, DD + M + M1, FD - M - DM1, DM - M1, DD + DM - M1, DD + M - DM1, DD - M - DF, FD + M1, TM1 + M1, FD - M, D - DM1, DD + M - DF, DM1 - DF, D + M + M1, TD - DM1, FD - TM1, DD - M + DM1, DM + M1, D + M - M1, DM + TM1, DD - M1 - DF};
		double[] Coords = new double[2];
		if (CalcCoords)
		{
			double Suml = 6288774 * Math.sin(Args[0]) + 1274027 * Math.sin(Args[1]) + 658314 * Math.sin(Args[2]) + 213618 * Math.sin(Args[3]) - E * 185116 * Math.sin(Args[4]) - 114332 * Math.sin(Args[5]) + 58793 * Math.sin(Args[6]) + E * 57066 * Math.sin(Args[7]) + 53322 * Math.sin(Args[8]) + E * 45758 * Math.sin(Args[9]) - E * 40923 * Math.sin(Args[10]) - 34720 * Math.sin(Args[11]) - E * 30383 * Math.sin(Args[12]) + 15327 * Math.sin(Args[13]) - 12528 * Math.sin(Args[14]) + 10980 * Math.sin(Args[15]) + 10675 * Math.sin(Args[16]) + 10034 * Math.sin(Args[17]) + 8548 * Math.sin(Args[18]) - E * 7888 * Math.sin(Args[19]) - E * 6766 * Math.sin(Args[20]) - 5163 * Math.sin(Args[21]) + E * 4987 * Math.sin(Args[22]) + E * 4036 * Math.sin(Args[23]) + 3994 * Math.sin(Args[24]) + 3861 * Math.sin(Args[25]) + 3665 * Math.sin(Args[26]) - E * 2689 * Math.sin(Args[27]) - 2602 * Math.sin(Args[28]) + E * 2390 * Math.sin(Args[29]) - 2348 * Math.sin(Args[30]) + E2 * 2236 * Math.sin(Args[31]) - E * 2120 * Math.sin(Args[32]) - E2 * 2069 * Math.sin(Args[33]) + E2 * 2048 * Math.sin(Args[34]) - 1773 * Math.sin(Args[35]) - 1595 * Math.sin(Args[36]) + E * 1215 * Math.sin(Args[37]) - 1110 * Math.sin(Args[38]) - 892 * Math.sin(Args[39]) - E * 810 * Math.sin(Args[40]) + E * 759 * Math.sin(Args[41]) - E2 * 713 * Math.sin(Args[42]) - E2 * 700 * Math.sin(Args[43]) + E * 691 * Math.sin(Args[44]) + E * 596 * Math.sin(Args[45]) + 549 * Math.sin(Args[46]) + 537 * Math.sin(Args[47]) + E * 520 * Math.sin(Args[48]) - 487 * Math.sin(Args[49]) - E * 399 * Math.sin(Args[50]) - 381 * Math.sin(Args[51]) + E * 351 * Math.sin(Args[52]) - 340 * Math.sin(Args[53]) + 330 * Math.sin(Args[54]) + E * 327 * Math.sin(Args[55]) - E2 * 323 * Math.sin(Args[56]) + E * 299 * Math.sin(Args[57]) + 294 * Math.sin(Args[58]) + 3958 * Math.sin(A1) + 1962 * Math.sin(L1 - F) + 318 * Math.sin(A2);		
			double Sumb = 5128122 * Math.sin(F) + 280602 * Math.sin(M1 + F) + 277693 * Math.sin(M1 - F) + 173237 * Math.sin(DD - F) + 55413 * Math.sin(DD - M1 + F) + 46271 * Math.sin(DD - M1 - F) + 32573 * Math.sin(DD + F) + 17198 * Math.sin(DM1 + F) + 9266 * Math.sin(DD + M1 - F) + 8822 * Math.sin(DM1 - F) + E * 8216 * Math.sin(DD - M - F) + 4324 * Math.sin(DD - DM1 - F) + 4200 * Math.sin(DD + M1 + F) - E * 3359 * Math.sin(DD + M - F) + E * 2463 * Math.sin(DD - M - M1 + F) + E * 2211 * Math.sin(DD - M + F) + E * 2065 * Math.sin(DD - M - M1 - F)- E * 1870 * Math.sin(M - M1 - F) + 1828 * Math.sin(FD - M1 - F) - E * 1794 * Math.sin(M + F) - 1749 * Math.sin(TF) - E * 1565 * Math.sin(M - M1 + F) - 1491 * Math.sin(D + F) - E * 1475 * Math.sin(M + M1 + F) - E * 1410 * Math.sin(M + M1 - F) - E * 1344 * Math.sin(M - F) - 1335 * Math.sin(D - F) + 1107 * Math.sin(TM1 + F) + 1021 * Math.sin(FD - F) + 833 * Math.sin(FD - M1 + F) + 777 * Math.sin(M1 - TF) + 671 * Math.sin(FD - DM1 + F) + 607 * Math.sin(DD - TF) + 596 * Math.sin(DD - DM1 - F) + E * 491 * Math.sin(DD - M + M1 - F) - 451 * Math.sin(DD - DM1 + F) + 439 * Math.sin(TM1 - F) + 422 * Math.sin(DD + DM1 + F) + 421 * Math.sin(DD - TM1 - F) - E * 366 * Math.sin(DD + M - M1 + F) - E * 351 * Math.sin(DD + M + F) + 331 * Math.sin(FD + F) + E * 315 * Math.sin(DD - M + M1 + F) + E2 * 302 * Math.sin(DD - DM - F) - 283 * Math.sin(M1 + TF) - E * 229 * Math.sin(DD + M + M1 - F) + E * 223 * Math.sin(D + M - F) + E * 223 * Math.sin(D + M + F) - E * 220 * Math.sin(M - DM1 - F) - E * 220 * Math.sin(DD + M - M1 - F) - 185 * Math.sin(D + M1 + F) + E * 181 * Math.sin(DD - M - DM1 - F) - E * 177 * Math.sin(M + DM1 + F) + 176 * Math.sin(FD - DM1 - F) + E * 166 * Math.sin(FD - M - M1 - F) - 164 * Math.sin(D + M1 - F) + 132 * Math.sin(FD + M1 - F) - 119 * Math.sin(D - M1 - F) + E * 115 * Math.sin(FD - M - F) + E2 * 107 * Math.sin(DD - DM + F) - 2235 * Math.sin(L1) + 382 * Math.sin(A3) + 175 * Math.sin(A1 - F) + 175 * Math.sin(A1 + F) + 127 * Math.sin(L1 - M1) - 115 * Math.sin(L1 + M1);
			//Вычисляем эклиптические координаты:
			double Lambda = L1 + RPD * (Suml / 1000000.0);
			double Beta = RPD * (Sumb / 1000000.0);
			//Переводим эклиптические координаты в экваториальные:
			double Epsilon = RPD * (23.43929111111111 - 0.01300416666666667 * T - 0.00000016388888889 * T2 + 0.00000050361111111 * T3);
			double Alpha = Math.atan2(Math.cos(Epsilon) * Math.sin(Lambda) - Math.tan(Beta) * Math.sin(Epsilon), Math.cos(Lambda));
			double Delta = Math.sin(Beta) * Math.cos(Epsilon) + Math.cos(Beta) * Math.sin(Epsilon) * Math.sin(Lambda); 
			Coords[0] = Alpha;
			Coords[1] = Delta;
		}
		else
		{
			//Вычисляем расстояние до Луны:
			double Sumr = -20905355 * Math.cos(Args[0]) - 3699111 * Math.cos(Args[1]) - 2955968 * Math.cos(Args[2]) - 569925 * Math.cos(Args[3]) + E * 48888 * Math.cos(Args[4]) - 3149 * Math.cos(Args[5]) + 246158 * Math.cos(Args[6]) - E * 152138 * Math.cos(Args[7]) - 170733 * Math.cos(Args[8]) - E * 204586  * Math.cos(Args[9]) - E * 129620 * Math.cos(Args[10]) + 108743 * Math.cos(Args[11]) + E * 104755 * Math.cos(Args[12]) + 10321 * Math.cos(Args[13]) + 79661 * Math.cos(Args[15]) - 34782 * Math.cos(Args[16]) - 23210 * Math.cos(Args[17]) - 21636 * Math.cos(Args[18]) + E * 24208 * Math.cos(Args[19]) + E * 30824 * Math.cos(Args[20]) - 8379 * Math.cos(Args[21]) - E * 16675 * Math.cos(Args[22]) - E * 12831 * Math.cos(Args[23]) - 10445 * Math.cos(Args[24]) - 11650 * Math.cos(Args[25]) + 14403 * Math.cos(Args[26]) - E * 7003 * Math.cos(Args[27]) + E * 10056 * Math.cos(Args[29]) + 6322 * Math.cos(Args[30]) - E2 * 9884 * Math.cos(Args[31]) + E * 5751 * Math.cos(Args[32]) - E2 * 4950 * Math.cos(Args[34]) + 4130 * Math.cos(Args[35]) - E * 3958 * Math.cos(Args[37]) + 3258 * Math.cos(Args[39]) + E * 2616 * Math.cos(Args[40]) - E * 1897 * Math.cos(Args[41]) - E2 * 2117 * Math.cos(Args[42]) + E2 * 2354 * Math.cos(Args[43]) - 1423 * Math.cos(Args[46]) - 1117 * Math.cos(Args[47]) - E * 1571 * Math.cos(Args[48]) - 1739 * Math.cos(Args[49]) - 4421 * Math.cos(Args[51]) + E2 * 1165 * Math.cos(Args[56]) + 8752 * Math.cos(Args[59]);
			double BigDelta = 385000.56 + Sumr / 1000.0;
			//Вычисляем equatorial horizontal parallax.
			double Paralax = Math.asin(6378.14 / BigDelta);
			Coords[0] = BigDelta;
			Coords[1] = Paralax;
		}
		return Coords;
	}
	//Публичный метод для определения расстояния до Луны в км по заданному календарю.
	public static double GetMoonDistance(Calendar cal)
	{
		GregorianDate GD_UTC = GetGregorianDateFromCalendar(cal, true);
		double JDE = GregorianDateToJulianDay(GD_UTC, true);
		return GetMoonCoordinatesOrDistance(JDE, false)[0];
	}
	//Вычисляем звёздное время.
	//В данном случае вычисляются среднее звёздное время (не учитывающие нутацию и остальное), т.е. погрешность... вродебы очень маленькая (0.1 секунда)
	//Метод возвращает значения в радианах!
	private static double GetSiderealTime(double JD)
	{
		double T = (JD - 2451545.0) / 36525.0;
		double T2 = T * T;
		return RPD * (280.46061837 + 360.98564736629 * (JD - 2451545.0) + 0.000387933 * T2 - T2 * T / 38710000.0) % DOUBLE_PI;
	}
	//Вычисляем время заката рассвета и зенита. Пока доступны Солнце и Луна, но в принципе можно задать любое небесное тело, главное знать его экваториальные координаты. Эти координаты получаются в теле switch при выборе типа объекта.
	//Широта и долгота выражена в градусах! Положительные восточные долготы и северные широты, противоположное соответственно отрицательное.
	private static Calendar GetRisingTransitOrSetting(Calendar CurTime, int ObjectType, int EventType, double Longitude, double Latitude)
	{
		GregorianDate GD_UTC = GetGregorianDateFromCalendar(CurTime, true);
		//Сбрасываем время текущей даты в полночь.
		GD_UTC.RealDay = Math.floor(GD_UTC.RealDay);
		int GDYear = GD_UTC.Year;
		int GDMonth = GD_UTC.Month;
		double GDRealDay = GD_UTC.RealDay;
		//Получаем текущий Юлианский день, но без учёта дельта T, так как для расчёта звёздного времени время нужно универсальное а не эфемеридное.
		double CurJD_0hUTC = GregorianDateToJulianDay(GD_UTC, false);
		//Получаем текущий Юлианский день, но уже с учётом дельта T, так как для расчёта экваториальных координат время нужно эфемеридное а не универсальное.
		double CurJD_0hET = GregorianDateToJulianDay(GD_UTC, true);
		Calendar OutCal;
		int sensor = 0;
		double Min_dm = 30.0 / 86400.0;		
		while (true)
		{
			//Получаем предыдущий Юлианский день с учётом дельта T.
			double PrevJD_0hET = CurJD_0hET - 1.0;
			//И следующий Юлианский день с учётом дельта T.
			double NextJD_0hET = CurJD_0hET + 1.0;
			double teta0 = GetSiderealTime(CurJD_0hUTC);			
			double[] Coords1, Coords2, Coords3;
			double h0;
			switch (ObjectType)
			{
				case OBJECT_SUN:
					h0 = GetSunHorisontHeightPreserve();
					Coords1 = GetSolarCoordinates(PrevJD_0hET);
					Coords2 = GetSolarCoordinates(CurJD_0hET);
					Coords3 = GetSolarCoordinates(NextJD_0hET);
				break;
				case OBJECT_MOON:
					//Вычисляем высоту горизонта Луны для CurJD_0hET + 0.5. Это может дать небольшую ошибку в редких случаях, когда заход или восход случится на не CurJD день, а на следующий день.
					h0 = GetMoonHorisontHeightPreserve(CurJD_0hET + 0.5);
					Coords1 = GetMoonCoordinatesOrDistance(PrevJD_0hET, true);
					Coords2 = GetMoonCoordinatesOrDistance(CurJD_0hET, true);
					Coords3 = GetMoonCoordinatesOrDistance(NextJD_0hET, true);
				break;
				default:
					Log.e("MyLog", "Invalid ObjectType argument");
					return null;
			}
			if (EventType != EVENT_RISE && EventType != EVENT_SETTING && EventType != EVENT_TRANSIT)
			{
				Log.e("MyLog", "Invalid EventType argument");
				return null;
			}
			//В Меюсе восточная долгата считается отрицательной. Но мы же русские, и для нас она положительна :).
			double L = -RPD * Longitude;
			double Phi = RPD * Latitude;
			/*Необходимо преобразовать прямое восхождение (альфа).
			* Так как между трёмя полученными прямыми восхождениями будет рассчитываться интерполяция.
			* Функция atan2 вычисляет альфу между -пи и +пи. Поэтому может так случиться, что одна альфа будет чуть меньше +пи, другая чуть больше -пи.
			* Математически углы альфа будут близки, но когда мы вычисляем интерполяцию, то попадаем на альфа около нуля.
			* Чтобы этого избежать, нужно сделать так, чтобы близкие углы альфа, но находящиеся в разных секторах были близки друг другу численно.
			* Для этого сделаем так, что если все три угла альфа находятся либо в тетьей либо в четвёртой четвртях, то к тем углам, которые лежат в четвёртой четверти и соответственно меньше нуля, прибавим 2*пи. 
			*/
			if (Math.abs(Coords1[0]) > HALF_PI && Math.abs(Coords2[0]) > HALF_PI && Math.abs(Coords3[0]) > HALF_PI)
			{
				if (Coords1[0] < 0) Coords1[0] += DOUBLE_PI;
				if (Coords2[0] < 0) Coords2[0] += DOUBLE_PI;
				if (Coords3[0] < 0) Coords3[0] += DOUBLE_PI;
			}
			double m = (Coords2[0] + L - teta0) / DOUBLE_PI;
			if (EventType != EVENT_TRANSIT)
			{
				double cosH0 = (Math.sin(h0) - Math.sin(Phi) * Math.sin(Coords2[1])) / (Math.cos(Phi) * Math.cos(Coords2[1]));
				if (cosH0 > 1 || cosH0 < -1)
				{
					Log.i(PublicConstantsAndMethods.MY_LOG_TAG, "Cannot find time of risin or setting or transit");
					return null;
				}
				double H0 = Math.acos(cosH0);
				switch (EventType)
				{
					case EVENT_RISE:
						m -= H0 / DOUBLE_PI;
					break;
					case EVENT_SETTING:
						m += H0 / DOUBLE_PI;
					break;
				}
			}
			if (m >= 1)
			{
				m -= 1.0;
			}
			else if (m < 0)
			{
				m += 1.0;
			}
			double DeltaT = GetTDMinusUTInDays(CurJD_0hET);
			double dm = 0;
			int i = 0;
			do
			{
				double teta = teta0 + RPD * 360.985647 * m;
				double n = m + DeltaT;
				double aAlpha = Coords2[0] - Coords1[0];
				double bAlpha = Coords3[0] - Coords2[0];
				double cAlpha = bAlpha - aAlpha;
				double Alpha = Coords2[0] + (n / 2.0) * (aAlpha + bAlpha + n * cAlpha);
				double H = teta - L - Alpha;
				if (EventType != EVENT_TRANSIT)
				{
					double aDelta = Coords2[1] - Coords1[1];
					double bDelta = Coords3[1] - Coords2[1];
					double cDelta = bDelta - aDelta;
					double Delta = Coords2[1] + (n / 2.0) * (aDelta + bDelta + n * cDelta);
					double h = Math.asin(Math.sin(Phi) * Math.sin(Delta) + Math.cos(Phi) * Math.cos(Delta) * Math.cos(H));
					dm = (h - h0) / (DOUBLE_PI * Math.cos(Phi) * Math.cos(Delta) * Math.sin(H));
				}
				else
				{
					//Здесь H должна быть между -Пи и +Пи.
					H %= DOUBLE_PI;
					if (H > Math.PI)
					{
						H -= DOUBLE_PI;
					}
					else if (H < -Math.PI)
					{
						H += DOUBLE_PI;
					}
					dm = -H / DOUBLE_PI;
				}
				m += dm;
				if (i >= 1000)
				{
					Log.e("MyLog", "Error in \"GetRisingTransitOrSetting\". Infinite cycle.");
					return null;
				}
				i++;
			} while (Math.abs(dm) > Min_dm);
			GD_UTC.RealDay += m;
			//Проверяем вдруг так случиться, что прибавка dm сделает m больше 1 или меньше 0
			if (!CheckCregorianDate(GD_UTC))
			{
				if (m >= 1)
				{
					if (GD_UTC.Month == 12)
					{
						GD_UTC.Month = 1;
						GD_UTC.Year++;
					}
					else
					{
						GD_UTC.Month++;					
					}
					GD_UTC.RealDay -= (Math.floor(GD_UTC.RealDay) - 1.0);
				}
				else if (m < 0)
				{				
					if (GD_UTC.Month == 1)
					{
						GD_UTC.Month = 12;
						GD_UTC.Year--;
					}
					else
					{
						GD_UTC.Month--;
					}
					GD_UTC.RealDay += GetNumberOfDaysInMonth(GD_UTC.Year, GD_UTC.Month);
				}
			}
			OutCal = GetCalendarFromGregorianDate(GD_UTC, true);
			//Существует такая ситуация, что искомое время события при переводе в локальное время оказывается в предыдущем или в следующем дне. Это мы должны отработать:
			if (OutCal.get(Calendar.DAY_OF_MONTH) != CurTime.get(Calendar.DAY_OF_MONTH))
			{
				switch(sensor)
				{
					case 0:
						sensor = -1;
						CurJD_0hET += 1.0;
						CurJD_0hUTC += 1.0;
						//Это нужно, т.к. если вверху, после проверки что прибавка dm сделает m больше 1 или меньше 0 у GD_UTC месяц или год изменились, то вернуть их обратно как был.
						GD_UTC.Month = GDMonth;
						GD_UTC.Year = GDYear;
						//***
						GD_UTC.RealDay = GDRealDay + 1.0;
						if (!CheckCregorianDate(GD_UTC))
						{
							if (GD_UTC.Month == 12)
							{
								GD_UTC.Month = 1;
								GD_UTC.Year++;
							}
							else
							{
								GD_UTC.Month++;					
							}
							GD_UTC.RealDay = 1.0;
						}
					break;
					case -1:
						sensor = 1;
						CurJD_0hET -= 2.0;
						CurJD_0hUTC -= 2.0;
						//Это нужно, т.к. если вверху, после проверки что прибавка dm сделает m больше 1 или меньше 0 у GD_UTC месяц или год изменились, то вернуть их обратно как был.
						GD_UTC.Month = GDMonth;
						GD_UTC.Year = GDYear;
						//***
						GD_UTC.RealDay = GDRealDay - 1.0;
						if (!CheckCregorianDate(GD_UTC))
						{
							if (GD_UTC.Month == 1)
							{
								GD_UTC.Month = 12;
								GD_UTC.Year--;
							}
							else
							{
								GD_UTC.Month--;
							}
							GD_UTC.RealDay = GetNumberOfDaysInMonth(GD_UTC.Year, GD_UTC.Month);
						}
					break;
					case 1:
						Log.i(PublicConstantsAndMethods.MY_LOG_TAG, "Cannot find time of risin or setting or transit");
					return null;
				}			
			}
			else
			{
				break;
			}
		}
		return OutCal;
	}
	//Получаем время восхода захода и зенита для солнца и луны при условии, что они не были получены ранее (чтоб циклы зря не гонять)
	public static Calendar GetRisingTransitOrSettingPreserve(Calendar CurTime, int ObjectType, int EventType, double Longitude, double Latitude)
	{
		int ObjectIndex = -1;
		int EventIndex = -1;
		//Выбираем тип небесного тела.
		switch (ObjectType)
		{
			case OBJECT_SUN:
				ObjectIndex = 0;
			break;
			case OBJECT_MOON:
				ObjectIndex = 1;
			break;
			default:
				Log.e("MyLog", "Invalid ObjectType argument");
				return null;
		}
		//Выбираем тип события.
		switch (EventType)
		{
			case EVENT_RISE:
				EventIndex = 0;
			break;
			case EVENT_TRANSIT:
				EventIndex = 1;
			break;
			case EVENT_SETTING:
				EventIndex = 2;
			break;
			default:
				Log.e("MyLog", "Invalid EventType argument");
				return null;
		}
		//По типу небесного тела и нужного нам события рассчитываем какой будет номер элемента сохранённого статического массива, содержащего время события.
		//Правила расчёта записаны сверху, там где эти массивы определены.
		int index = EventIndex + ObjectIndex * 3;
		if (CurTime.get(Calendar.YEAR) == mObjectEventYear[index] && CurTime.get(Calendar.DAY_OF_YEAR) == mObjectEventDaysOfYear[index] && Longitude == mObjectEventLongitude[index] && Latitude == mObjectEventLatitude[index])
		{
			return mObjectEventTimes[index];
		}
		else
		{
			mObjectEventTimes[index] = GetRisingTransitOrSetting(CurTime, ObjectType, EventType, Longitude, Latitude);
			mObjectEventDaysOfYear[index] = CurTime.get(Calendar.DAY_OF_YEAR);
			mObjectEventYear[index] = CurTime.get(Calendar.YEAR);
			mObjectEventLongitude[index] = Longitude;
			mObjectEventLatitude[index] = Latitude;
			return mObjectEventTimes[index];
		}
	}
	//Получаем высоту небесного тела над горизонтом или его азимут от юга. Всё в радианах.
	//Широта и долгота выражена в градусах! Положительные восточные долготы и северные широты, противоположное соответственно отрицательное.
	public static double GetObjectHeightOrSouthAzimuth(Calendar CurTime, int ObjectType, double Longitude, double Latitude, boolean CalcHeight)
	{
		//Получаем время в нашем формате, приведённое в UTC.
		GregorianDate GD_UTC = GetGregorianDateFromCalendar(CurTime, true);
		//Получаем текущий Юлианский день, но без учёта дельта T, так как для расчёта звёздного времени время нужно универсальное а не эфемеридное.
		double JD = GregorianDateToJulianDay(GD_UTC, false);
		//Получаем текущий Юлианский день, но уже с учётом дельта T, так как для расчёта экваториальных координат время нужно эфемеридное а не универсальное.
		double JDE = GregorianDateToJulianDay(GD_UTC, true);
		double[] Coords;
		double teta0 = GetSiderealTime(JD);
		switch (ObjectType)
		{
			case OBJECT_SUN:
				Coords = GetSolarCoordinates(JDE);
			break;
			case OBJECT_MOON:
				Coords = GetMoonCoordinatesOrDistance(JDE, true);
			break;
			default:
				Log.e("MyLog", "Invalid ObjectType argument");
				return 0.0;
		}
		//Почему минус в долготе - смотри в метод, вычисляющий время восхода/зенита/заката.
		double L = -RPD * Longitude;
		double Phi = RPD * Latitude;
		double H = teta0 - L - Coords[0];
		if (CalcHeight)
		{
			return Math.asin(Math.sin(Phi) * Math.sin(Coords[1]) + Math.cos(Phi) * Math.cos(Coords[1]) * Math.cos(H));
		}
		else
		{
			return Math.atan2(Math.sin(H), Math.cos(H) * Math.sin(Phi) - Math.tan(Coords[1]) * Math.cos(Phi));
		}
	}
	//Вычисляем длительность светового (лунного или солнечного, а может и звёздного). Время записываем в массив: часы, минуты, секунды, милисекунды.	
	//Получения высоты небесных объектов в зените
	public static double GetObjectTransitHeightPreserve(Calendar CurTime, int ObjectType, double Longitude, double Latitude)
	{
		switch (ObjectType)
		{
			case OBJECT_SUN:
				if (Longitude != mOblectTransitLongitude[0] || Latitude != mOblectTransitLatitude[0] || CurTime.get(Calendar.YEAR) != mObjectTransitHeightDate[0] || CurTime.get(Calendar.DAY_OF_YEAR) != mObjectTransitHeightDate[1])
				{
					mObjectTransitHeightDate[0] = CurTime.get(Calendar.YEAR);
					mObjectTransitHeightDate[1] = CurTime.get(Calendar.DAY_OF_YEAR);
					mOblectTransitLongitude[0] = Longitude;
					mOblectTransitLatitude[0] = Latitude;
					Calendar TransitTime = GetRisingTransitOrSettingPreserve(CurTime, ObjectType, EVENT_TRANSIT, Longitude, Latitude);
					if (TransitTime != null)
					{
						mObjectTransitHeight[0] = GetObjectHeightOrSouthAzimuth(TransitTime, ObjectType, Longitude, Latitude, true);
					}
					else
					{
						mObjectTransitHeight[0] = Double.MAX_VALUE;
					}
					return mObjectTransitHeight[0];
				}
				else
				{
					return mObjectTransitHeight[0];
				}
			case OBJECT_MOON:
				if (Longitude != mOblectTransitLongitude[1] || Latitude != mOblectTransitLatitude[1] || CurTime.get(Calendar.YEAR) != mObjectTransitHeightDate[2] || CurTime.get(Calendar.DAY_OF_YEAR) != mObjectTransitHeightDate[3])
				{
					mObjectTransitHeightDate[2] = CurTime.get(Calendar.YEAR);
					mObjectTransitHeightDate[3] = CurTime.get(Calendar.DAY_OF_YEAR);
					mOblectTransitLongitude[1] = Longitude;
					mOblectTransitLatitude[1] = Latitude;
					Calendar TransitTime = GetRisingTransitOrSettingPreserve(CurTime, ObjectType, EVENT_TRANSIT, Longitude, Latitude);
					if (TransitTime != null)
					{
						mObjectTransitHeight[1] = GetObjectHeightOrSouthAzimuth(TransitTime, ObjectType, Longitude, Latitude, true);
					}
					else
					{
						mObjectTransitHeight[1] = Double.MAX_VALUE;
					}
					return mObjectTransitHeight[1];
				}
				else
				{
					return mObjectTransitHeight[1];
				}
			default:
				Log.e("MyLog", "Invalid ObjectType argument");
				return 0.0;
		}
	}
	//Получаем в радианах высоту Солнца, в которой считается закат или рассвет.
	//Данный метод на самом деле ничего не делает, но он существует, чтобы соблюдалась аналогия с Луной, где уже нужны вычисления. 
	public static double GetSunHorisontHeightPreserve()
	{
		return mSunHorisontHeight;
	}
	//Получаем в радианах высоту Луны, в которой считается закат или рассвет.
	//Для этого вычисляется горизонтальный паралакс.
	public static double GetMoonHorisontHeightPreserve(double JDE)
	{
		int intJDE = (int)Math.floor(JDE);
		if (intJDE != mDateOfMoonHorisontHeight)
		{
			mDateOfMoonHorisontHeight = intJDE;
			mMoonHorisontHeight = 0.7275 * GetMoonCoordinatesOrDistance(JDE, false)[1] - RPD * (34.0 / 60.0);
			return mMoonHorisontHeight;
		}
		else
		{
			return mMoonHorisontHeight;
		}
	}
	//Вычисляем длительность светового дня в миллисекундах.
	public static long GetLengthOfDaylightInMillis(Calendar CurTime, int ObjectType, double Longitude, double Latitude)
	{
		Calendar CalRising = GetRisingTransitOrSettingPreserve(CurTime, ObjectType, EVENT_RISE, Longitude, Latitude);
		Calendar CalSetting = GetRisingTransitOrSettingPreserve(CurTime, ObjectType, EVENT_SETTING, Longitude, Latitude);
		if (CalRising == null || CalSetting == null)
		{
			return Long.MAX_VALUE;
		}
		else
		{
			return CalSetting.getTimeInMillis() - CalRising.getTimeInMillis();
		}
	}
	//Если время выражено в миллисекундах, то с помощью этой функции его можно преобразовать в часы минуты секунды и миллисикунды.
	public static int[] ConvertMillisToHHMMSSsss(long Millis)
	{
		double hh = (double)Millis / 3600000.0;
		double h = Math.floor(hh);
		double mm = (hh - h) * 60.0;
		double m = Math.floor(mm);
		double ss = (mm - m) * 60.0;
		double s = Math.floor(ss);
		double msms = (ss - s) * 1000.0;
		double ms = Math.floor(msms);
		int[] TimeArray = new int[4];
		TimeArray[0] = (int)h;
		TimeArray[1] = (int)m;
		TimeArray[2] = (int)s;
		TimeArray[3] = (int)ms;
		return TimeArray; 
	}
	//~New Вычисление Православной Пасхи.
	public static Calendar GetOrthodoxEaster(int year)
	{
		if (year < 1583) return null;
		Calendar out = Calendar.getInstance();
		int a = year % 19;
		int b = year % 4;
		int c = year % 7;
		int d = (19 * a + 15) % 30;
		int e = (2 * b + 4 * c + 6 * d + 6) % 7;
		if (d + e >= 10)
		{
			out.set(Calendar.MONTH, Calendar.APRIL);
			out.set(Calendar.DAY_OF_MONTH, d + e - 9);
		}
		else
		{
			out.set(Calendar.MONTH, Calendar.MARCH);
			out.set(Calendar.DAY_OF_MONTH, 22 + d + e);
		}
		out.set(Calendar.HOUR_OF_DAY, 0);
		out.set(Calendar.MINUTE, 0);
		out.set(Calendar.SECOND, 0);
		out.set(Calendar.MILLISECOND, 0);
		out.add(Calendar.DAY_OF_YEAR, 13);
		return out;
	}
	//***************************************************************************************************************
	//ПРЕОБРАЗОВАНИЕ ДАТ
	//Конвертация Грегорианской даты (UTC) в Юлианский день (по UTC или по эфемеридному времени на выбор)
	private static double GregorianDateToJulianDay(GregorianDate GD_UTC, boolean ConvertUTtoET)
	{
		if (!CheckCregorianDate(GD_UTC)) return -1.0;
		double yy, mm;
		if (GD_UTC.Month == 1 || GD_UTC.Month == 2)
		{
			yy = (double)(GD_UTC.Year - 1);
			mm = (double)(GD_UTC.Month + 12);
		}
		else
		{
			yy = (double)GD_UTC.Year;
			mm = (double)GD_UTC.Month;
		}
		double B = 0.0;
		if ((GD_UTC.Year > 1582) || (GD_UTC.Year == 1582 && GD_UTC.Month > 10) || (GD_UTC.Year == 1582 && GD_UTC.Month == 10 && GD_UTC.RealDay > 15.0))
		{
			double A = Math.floor(yy / 100.0);
			B = 2.0 - A + Math.floor(A / 4.0);
		}
		double JD = Math.floor(365.25 * (yy + 4716)) + Math.floor(30.6001 * (mm + 1)) + GD_UTC.RealDay + B - 1524.5;
		if (ConvertUTtoET)
		{
			return  JD + GetTDMinusUTInDays(JD);
		}
		else
		{
			return JD;
		}
	}
	//Конвертация Юлианского дня (Эфемеридное время) в Григорианскую дату (UTC)
	private static GregorianDate JulianDayToGregorianDate(double JDE_preserve, boolean ConvertETtoUT)
	{
		double JD;
		if (ConvertETtoUT)
		{
			JD = JDE_preserve - GetTDMinusUTInDays(JDE_preserve);
		}
		else
		{
			JD = JDE_preserve;
		}
		if (JD < 0.0) return null;
		JD += 0.5;
		double Z = Math.floor(JD);
		double F = JD - Z;
		double A;
		if (Z < 2299161)
		{
			A = Z;
		}
		else
		{
			double alpha = Math.floor((Z - 1867216.25) / 36524.25);
			A = Z + 1 + alpha - Math.floor(alpha / 4.0);
		}
		double B = A + 1524;
		double C = Math.floor((B - 122.1) / 365.25);
		double D = Math.floor(365.25 * C);
		double E = Math.floor((B - D) / 30.6001);
		double RealDay = B - D - Math.floor(30.6001 * E) + F;
		int IntE = (int)E;
		int Month = (IntE < 14) ? (IntE - 1) : (IntE - 13);
		int IntC = (int)C;
		int Year = (Month > 2) ? (IntC - 4716) : (IntC - 4715);
		return new GregorianDate(Year, Month, RealDay);
	}
	//Вычисление разницы в днях между динамическим (или приближённо эфемеридным) временем (используется астрономами) и гражданским универсальным временем
	//Используются данные НАСА: http://eclipse.gsfc.nasa.gov/SEhelp/deltatpoly2004.html
	//Стандартное отклонение: http://eclipse.gsfc.nasa.gov/SEhelp/uncertainty2004.html
	private static double GetTDMinusUTInDays(double JD)
	{	
		double dTsec;
		if (JD < 1538433.0)
		{
			double u = (JD - 2385801.0) / 36525.0;
			double u2 = u * u;
			dTsec = -20.0 + 32.0 * u2;
		}
		else if (JD < 1903683.0)
		{
			double u = JD / 36525.0;
			double u2 = u * u;
			double u3 = u2 * u;
			double u4 = u3 * u;
			double u5 = u4 * u;
			double u6 = u5 * u;
			dTsec = 10583.6 - 1014.41 * u + 33.78311 * u2 - 5.952053 * u3 - 0.1798452 * u4 + 0.022174192 * u5 + 0.0090316521 * u6;
		}
		else if (JD < 2305448.0)
		{
			double u = (JD - 2086308.0) / 36525.0;
			double u2 = u * u;
			double u3 = u2 * u;
			double u4 = u3 * u;
			double u5 = u4 * u;
			double u6 = u5 * u;
			dTsec = 1574.2 - 556.01 * u + 71.23472 * u2 + 0.319781 * u3 - 0.8503463 * u4 - 0.005050998 * u5 + 0.0083572073 * u6;
		}
		else if (JD < 2341973.0)
		{
			double t = (JD-2305448.0) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			dTsec = 120.0 - 0.9808 * t - 0.01532 * t2 + t3 / 7129.0;
		}
		else if (JD < 2378497.0)
		{
			double t = (JD - 2341973.0) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			double t4 = t3 * t;
			dTsec = 8.83 + 0.1603 * t - 0.0059285 * t2 + 0.00013336 * t3 - t4 / 1174000.0;
		}
		else if (JD < 2400411.0)
		{
			double t = (JD - 2378497.0) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			double t4 = t3 * t;
			double t5 = t4 * t;
			double t6 = t5 * t;
			double t7 = t6 * t;
			dTsec = 13.72 - 0.332447 * t + 0.0068612 * t2 + 0.0041116 * t3 - 0.00037436 * t4 + 0.0000121272 * t5 - 0.0000001699 * t6 + 0.000000000875 * t7;
		}
		else if (JD < 2415021.0)
		{
			double t = (JD - 2400411) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			double t4 = t3 * t;
			double t5 = t4 * t;
			dTsec = 7.62 + 0.5737 * t - 0.251754 * t2 + 0.01680668 * t3 - 0.0004473624 * t4 + t5 / 233174.0;
		}
		else if (JD < 2422325.0)
		{
			double t = (JD - 2415021) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			double t4 = t3 * t;
			dTsec = -2.79 + 1.494119 * t - 0.0598939 * t2 + 0.0061966 * t3 - 0.000197 * t4;
		}
		else if (JD < 2429996.0)
		{
			double t = (JD - 2422325.0) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			dTsec = 21.20 + 0.84493 * t - 0.076100 * t2 + 0.0020936 * t3;
		}
		else if (JD < 2437301.0)
		{
			double t = (JD - 2433283.0) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			dTsec = 29.07 + 0.407 * t- t2 / 233.0 + t3 / 2547.0;
		}
		else if (JD < 2446432.0)
		{
			double t = (JD - 2442414.0) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			dTsec = 45.45 + 1.067 * t - t2 / 260.0 - t3 / 718.0;
		}
		else if (JD < 2453372.0)
		{
			double t = (JD - 2451545) / 365.25;
			double t2 = t * t;
			double t3 = t2 * t;
			double t4 = t3 * t;
			double t5 = t4 * t;
			dTsec = 63.86 + 0.3345 * t - 0.060374 * t2 + 0.0017275 * t3 + 0.000651814 * t4 + 0.00002373599 * t5;
		}
		else if (JD < 2469808.0)
		{
			double t = (JD - 2451545.0) / 365.25;
			double t2 = t * t;
			dTsec = 62.92 + 0.32217 * t + 0.005589 * t2;
		}
		else if (JD < 2506332.0)
		{
			double u = (JD-2385801.0) / 36525.0;
			double u2 = u * u;
			double t = (2506332.0 - JD) / 365.25;
			dTsec = -20.0 + 32.0 * u2 - 0.5628 * t;
		}
		else
		{
			double u = (JD - 2385801.0) / 36525.0;
			double u2 = u * u;
			dTsec = -20.0 + 32.0 * u2;
		}
		return dTsec / 86400.0;
	}
	//Преобразование переданного календаря в календарь по UTC времени. 
	private static Calendar CreateCalendarWithUTCTime(Calendar cal)
	{
		Calendar NewCal = (Calendar)cal.clone();
		String UTCTimeZoneID = "UTC";
		if (!NewCal.getTimeZone().getID().equals(UTCTimeZoneID))
		{			
			int UTCOffSet = NewCal.get(Calendar.ZONE_OFFSET) + NewCal.get(Calendar.DST_OFFSET);
			NewCal.setTimeInMillis(NewCal.getTimeInMillis() - UTCOffSet);
			NewCal.getTimeZone().setID(UTCTimeZoneID);
		}
		return NewCal;
	}
	//Преобразование переданного календаря (в UTC) в календарь по локальному времени. 
	private static Calendar CreateCalendarWithLocalTime(Calendar cal)
	{
		Calendar NewCal = Calendar.getInstance();
		String UTCTimeZoneID = "UTC";
		if (cal.getTimeZone().getID().equals(UTCTimeZoneID))
		{
			int UTCOffSet = NewCal.get(Calendar.ZONE_OFFSET) + NewCal.get(Calendar.DST_OFFSET);
			NewCal.setTimeInMillis(cal.getTimeInMillis() + UTCOffSet);
		}
		else
		{
			NewCal = (Calendar)cal.clone();
		}
		return NewCal;
	}
	//Преобразование календаря в используемый нами формат даты (Год, месяц и вещественный день).
	//Второй параметр - флаг, показывающий нужно ли конвертировать дату в календаре в формат UTC времени.
	private static GregorianDate GetGregorianDateFromCalendar(Calendar cal, boolean ConvertToUTC)
	{
		Calendar NewCal = ConvertToUTC ? CreateCalendarWithUTCTime(cal) : cal;
		int Year = NewCal.get(Calendar.YEAR);
		int Month = NewCal.get(Calendar.MONTH) + 1;
		double RealDay = (double)NewCal.get(Calendar.DAY_OF_MONTH) + (double)(NewCal.get(Calendar.HOUR_OF_DAY) * 3600000 + NewCal.get(Calendar.MINUTE) * 60000 + NewCal.get(Calendar.SECOND) * 1000 + NewCal.get(Calendar.MILLISECOND)) / 86400000.0;
		return new GregorianDate(Year, Month, RealDay);
	}
	//Преобразование используемого нами формата даты (Год, месяц и вещественный день) в календарь.
	//Второй параметр - флаг, показывающий нужно ли конвертировать дату в календаре в локальный формат времени из UTC.
	private static Calendar GetCalendarFromGregorianDate(GregorianDate GD_UTC, boolean ConvertToLocal)
	{
		Calendar cal = Calendar.getInstance();
		String UTCTimeZoneID = "UTC";
		cal.getTimeZone().setID(UTCTimeZoneID);
		cal.set(Calendar.YEAR, GD_UTC.Year);
		cal.set(Calendar.MONTH, GD_UTC.Month - 1);
		double d = Math.floor(GD_UTC.RealDay);
		double hh = (GD_UTC.RealDay - d) * 24.0;
		double h = Math.floor(hh);
		double mm = (hh - h) * 60.0;
		double m = Math.floor(mm);
		double ss = (mm - m) * 60.0;
		double s = Math.floor(ss);
		double msms = (ss - s) * 1000.0;
		double ms = Math.floor(msms);
		cal.set(Calendar.DAY_OF_MONTH, (int)d);
		cal.set(Calendar.HOUR_OF_DAY, (int)h);
		cal.set(Calendar.MINUTE, (int)m);
		cal.set(Calendar.SECOND, (int)s);
		cal.set(Calendar.MILLISECOND, (int)ms);
		if (ConvertToLocal)
		{
			return CreateCalendarWithLocalTime(cal);
		}
		else
		{
			return cal;
		}
	}
	//Получение числа дней в месяце
	private static double GetNumberOfDaysInMonth(int Year, int Month)
	{
		double DaysInFebrary = 0; 
		if (Month == 2)
		{
			boolean LeapYear = IsLeapYear(Year);
			DaysInFebrary = LeapYear ? 29.0 : 28.0;
			return DaysInFebrary;
		}
		else
		{
			double[] DaysInMonth = {31.0, DaysInFebrary, 31.0, 30.0, 31.0, 30.0, 31.0, 31.0, 30.0, 31.0, 30.0, 31.0};
			return DaysInMonth[Month - 1];
		}
	}
	//Класс представляющий собой структуру данных, содержащую Год, месяц и вещественный день.
	private static final class GregorianDate
	{
		public int Year = -1;
		public int Month = -1;
		public double RealDay = -1.0;
		
		public GregorianDate(int Y, int M, double RD)
		{
			Year = Y;
			Month = M;
			RealDay = RD;
		}
	}
	//Проверяем заполнена ли наша структура подходящими данными.
	private static boolean CheckCregorianDate(GregorianDate GD_UTC)
	{
		boolean flag;
		//Пишется в отдельном if'е т.к. мы должны сначала убедиться, наш класс с датой существует, а уже потом проверять всё остальное.
		if (GD_UTC == null)
		{
			flag = false;
		}
		else if (GD_UTC.Year < -1500 || GD_UTC.Year > 3000 || GD_UTC.Month < 1 || GD_UTC.Month > 12 || GD_UTC.RealDay < 1.0 || (GD_UTC.Year == 1582 && GD_UTC.Month == 10 && GD_UTC.RealDay > 5.0 && GD_UTC.RealDay < 15.0))
		{
			flag = false;
		}
		//Пишется в отдельном if'е т.к. мы должны сначала убедиться, что год и месяц заданы верно (мы их передаём в метод GetNumberOfDaysInMonth), а уже потом проверять число дней в месяце. 
		else if (GD_UTC.RealDay > GetNumberOfDaysInMonth(GD_UTC.Year, GD_UTC.Month) + 1.0)
		{
			flag = false;
		}
		else
		{
			flag = true;
		}
		if (!flag) Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error. Incorrect date.");
		return flag;
	}
}